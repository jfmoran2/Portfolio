
# Image Segmentation using EM - Summary Report
#
# John Moran
#
# **Environment:** OSX, Jupyter Notebook, Python  
# **Libraries:** numpy, scipy
# 

# **Part1:** The EM algorithm was applied to the mixture of normal distributions model to cluster the image pixels. Each of the three supplied test images were segmented to 10,20 and 50 segments where each pixel was replaced with the mean color of the closest segment.
# 
# The results below are extremely close to the test images supplied.
# 
# **Part2
# :** The sunset image was segmented to 20 segments using 5 different start points. The results have subtle but noticeable differences, and the number of iterations taken to converge varies from 62 to 101.
# 
# **Citations: **
# 
# All code for this homework is original. Stack Overflow and other sites were used for miscellaneous programming technicalities.
# 
# ** **

# ## Problem 2 - EM Image Segmentation



import numpy as np
from imageio import imread, imwrite
import matplotlib.pyplot as plt
from scipy.spatial import distance
get_ipython().run_line_magic('matplotlib', 'inline')

# set Jupyter to display ALL output from a cell (not just last output)
from IPython.core.interactiveshell import InteractiveShell
InteractiveShell.ast_node_interactivity = 'all'

np.set_printoptions(linewidth=115, threshold=5000, edgeitems=10, suppress=True, precision=6)


def expectation(pixels, mu_s, pi_s):

    distances = distance.cdist(pixels, mu_s, 'euclidean')
    dmin_sq = np.square(np.amin(distances, axis=1))[:,np.newaxis]    # p249 dmin is distsnce to nearest cluster
    
    exponent = np.zeros((num_pixels, num_clusters))
    for i in range(num_clusters):
        exponent[:,i] = np.sum( (pixels - mu_s[i,:])**2, axis=1)     # p249 formula: -(x - mu)^2 
    exponent = - (exponent-dmin_sq) / 2
    
    numerator = np.dot( np.exp(exponent), np.diag(pi_s) )      # p249 formula numerator
    denominator = numerator.sum(axis=1)[:, np.newaxis]         # p249 formula denominator sum(pi * e^-[(x-mu)^2]/2)
    w_ij = numerator / denominator

    q = np.sum(exponent * w_ij)                                # calculate q in case needed (not used)
    return q, w_ij

def maximization(pixels, w_ij):                                # mu and pi formulas from p247, 10.3.2
    for i in range(num_clusters):
        numerator = np.sum(pixels * w_ij[:,i][:,np.newaxis], axis=0)
        denominator = np.sum(w_ij[:,i])
        mu_s[i,:] = numerator / denominator
        pi_s[i] = np.sum(w_ij[:,i]) / num_pixels
    return mu_s, pi_s

def final_pic(w_ij, height, width):              # assemble final image from mu_s of nearest cluster
    final_img = np.zeros((height, width, 3))
    for i in range(height):
        for j in range(width):
            mu_seg = w_ij[i * width + j,:].argmax()
            final_img[i,j,:] = mu_s[mu_seg,:]
 
    final_img = np.round(final_img).astype(np.uint8)
    _ = plt.figure(figsize=(16,9));  _ = plt.imshow(final_img); _ = plt.show();
    imwrite(filename+'_'+str(num_clusters)+'_segments.jpg', final_img) # save image to disk if needed


IMAGES = ['RobertMixed03','smallstrelitzia','smallsunset']
STOP_LIMIT = 25
MAX_ITERATIONS = 400

# main processing loop for files and number of clusters
for filename in IMAGES:
    for num_clusters in [10,20,50]:
        print("Processing", filename, ", clusters=", num_clusters)
        
        pic = imread(filename+'.jpg')
        height, width, colours = pic.shape          # get picture dimensions
        num_pixels = height * width
        pixels = pic.reshape((num_pixels, colours)) # reshape into one row for each pixel

        pi_s = np.full((num_clusters), 1 / num_clusters) # initialize pi_s to small even spread of probabilities
        w_ij = np.zeros((num_pixels, num_clusters))

        np.random.seed(1)
        mu_s = np.random.rand(num_clusters, colours)     # initialize mu_s randomly

        iteration, MAD_mu_s = 1,99
        while MAD_mu_s >= STOP_LIMIT and iteration < MAX_ITERATIONS:
            previous_mu_s = mu_s.copy()

            # Expectation-Maximization
            q, w_ij    = expectation(pixels, mu_s, pi_s) # pixels is same as x_ik in HW7P1
            mu_s, pi_s = maximization(pixels, w_ij)

            MAD_mu_s = np.abs(previous_mu_s - mu_s).sum() # mean absolute difference in mu_s is stopping criteria
#             print ('Iteration', str(iteration).rjust(2), ', Q= %7.1f' % q, ', delta mu_s= ', "%7.2f" % MAD_mu_s)
            print('Iter:', iteration, end=', ')
            iteration += 1

        final_pic(w_ij, height, width)


#%%

%%time
# Part 2 - Special Test image, 20 clusters, different starting points
IMAGES = ['smallsunset']
STOP_LIMIT = 25
MAX_ITERATIONS = 400
num_clusters = 20

# main processing loop for 5 different starting points (random seeds)
filename = IMAGES[0]
for seed in range(1,500,100):
    print("Processing", filename, ", clusters=", num_clusters, 'seed=',seed)

    pic = imread(filename+'.jpg')
    height, width, colours = pic.shape          # get picture dimensions
    num_pixels = height * width
    pixels = pic.reshape((num_pixels, colours)) # reshape into one row for each pixel

    pi_s = np.full((num_clusters), 1 / num_clusters) # initialize pi_s to small even spread of probabilities
    w_ij = np.zeros((num_pixels, num_clusters))

    np.random.seed(seed)
    mu_s = np.random.rand(num_clusters, colours)     # initialize mu_s randomly

    iteration, MAD_mu_s = 1,99
    while MAD_mu_s >= STOP_LIMIT and iteration < MAX_ITERATIONS:
        previous_mu_s = mu_s.copy()

        # Expectation-Maximization
        q, w_ij    = expectation(pixels, mu_s, pi_s) # pixels is same as x_ik in HW7P1
        mu_s, pi_s = maximization(pixels, w_ij)

        MAD_mu_s = np.abs(previous_mu_s - mu_s).sum() # mean absolute difference in mu_s is stopping criteria
#             print ('Iteration', str(iteration).rjust(2), ', Q= %7.1f' % q, ', delta mu_s= ', "%7.2f" % MAD_mu_s)
        print('Iter:', iteration, end=', ')
        iteration += 1

    final_pic(w_ij, height, width)
