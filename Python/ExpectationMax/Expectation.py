import numpy as np
from scipy.special import logsumexp
from scipy.cluster.vq import kmeans
import matplotlib.pyplot as plt
get_ipython().run_line_magic('matplotlib', 'inline')

# set Jupyter to display ALL output from a cell (not just last output)
from IPython.core.interactiveshell import InteractiveShell
InteractiveShell.ast_node_interactivity = 'all'

np.set_printoptions(linewidth=115, threshold=5000, edgeitems=10, suppress=True)

# Subscript standards
# i = index/subscript referring to document
# j = index/subscript referring to topic/cluster
# k = index/subscript referring to word


# ## Problem 1 - EM Topic Models

def expectation(x_ik, mu_s, pi_s):

    sum_k = np.dot(x_ik, np.log(mu_s).T)  # the innermost sum on p247 10.3.3 (second Q formula)
    
    sum_ij  = np.zeros((NUM_DOCS, NUM_CLUSTERS))             # the second innermost sum
    for ij in range(NUM_CLUSTERS):
         sum_ij[:,ij] = sum_k[:,ij] + np.log(pi_s[ij])
    
    sum_ij_norm = sum_ij - sum_ij.max(axis=1)[:, np.newaxis]
 
    log_sum_exp = np.zeros(NUM_DOCS)
    
    for i in range(NUM_DOCS):
        log_sum_exp[i] = logsumexp(sum_ij_norm[i,:])
    
    log_w_ij = sum_ij_norm - log_sum_exp[:, np.newaxis]
    w_ij = np.exp(log_w_ij)                             # return from the log world

    for i in range(NUM_DOCS):                           # normalize the w_ijs
        w_ij[i,:] = w_ij[i,:] / sum(w_ij[i,:])   

    q = np.sum(sum_ij * w_ij)
    
    return q, w_ij
    

def maximization(x, w_ij):
    mu_s = np.zeros((NUM_CLUSTERS, NUM_VOCAB_WORDS))
    pi_s = np.zeros(NUM_CLUSTERS)
    
    for j in range(NUM_CLUSTERS): 
        numerator   = np.sum( x * w_ij[:,j,np.newaxis], axis=0) + A_TINY_BIT*100  # avoid divide by zero errors
        denominator = np.sum( np.sum(x, axis=1) *  w_ij[:,j]    + A_TINY_BIT*100) # incl in sum to avoid distortion
        mu_s[j] = numerator / denominator
        pi_s[j]  = np.sum(w_ij[:,j]) / NUM_DOCS
        
    return mu_s, pi_s


NUM_CLUSTERS = 30
MAX_ITERATIONS = 20
STOP_LIMIT = 1e-8
A_TINY_BIT = 1e-6

print("Loading Data File...")
documents = np.loadtxt('docword.nips.txt', dtype=int, skiprows=3)

NUM_DOCS, NUM_VOCAB_WORDS, _ = np.max(documents, axis=0)
print('Num Documents=', NUM_DOCS,', Num Vocab Words=', NUM_VOCAB_WORDS, ', Num Clusters=', NUM_CLUSTERS)

print("\nInitialising Arrays...")
x_ik = np.zeros((NUM_DOCS, NUM_VOCAB_WORDS))
mu_s = np.zeros((NUM_CLUSTERS, NUM_VOCAB_WORDS))
pi_s = np.full((NUM_CLUSTERS), 1 / NUM_CLUSTERS) # initialize pi_s to small even spread of probabilities
print("X_ik is Num Docs by Num Vocab Words:", x_ik.shape)
print("mu_s is Num Clusters by Num Vocab Words:", mu_s.shape)
print("pi_s is Num Clusters:", pi_s.shape)

# build x_ik matrix from documents
for row in documents:
    x_ik[row[0]-1][row[1]-1] = row[2]

# get initial cluster probabilities from k-means
np.random.seed(2)
#centroids, _ = kmeans(x_ik, k_or_guess=NUM_CLUSTERS, iter=15) # are we allowed to use this?
centroids = np.random.rand(NUM_CLUSTERS, NUM_VOCAB_WORDS)   # if not then we can use this, slightly worse q

# initialize mu_s to initial cluster centres
centroids += A_TINY_BIT   # avoid divide by zero errors
for j in range(NUM_CLUSTERS):
    mu_s[j] = (centroids[j])/ np.sum(centroids[j])


# main E-M loop
print("\nStarting Expectation Maximization Loop...")
iteration, delta_q, q  = 1,1,1
while abs(delta_q / q) >= STOP_LIMIT and iteration < MAX_ITERATIONS:
    previous_q = q
    
    # do Expectation-Maximization
    q, w_ij = expectation(x_ik, mu_s, pi_s)
    mu_s, pi_s = maximization(x_ik, w_ij)
    
    # calculate and print change in q
    delta_q = q - previous_q 
    print ('Iteration', str(iteration).rjust(1), ', Q= %11.1f' % q, ', Q improvement= ', "%14.8f" % abs(delta_q))
    iteration += 1


print('\nHighest probablity words for each topic')
vocab = [line.strip() for line in open("vocab.nips.txt")]
for i in range(NUM_CLUSTERS):
    top10 = mu_s[i].argsort()[-10:][::-1]
    print ('Topic', str(i).rjust(2), ':', ', '.join([vocab[i] for i in top10]))
        
# plot probabilities for each topic
_ = plt.figure(figsize=(10,7))
_ = plt.bar(range(NUM_CLUSTERS), pi_s); _ = plt.grid()
_ = plt.title('Probability of Each Topic'); _ = plt.xlabel('Topic'); _ = plt.ylabel('Probability')
