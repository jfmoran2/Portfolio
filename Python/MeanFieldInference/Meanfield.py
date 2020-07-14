#-----------------------------------------------------------------------------------
# Submitter: John Moran
# Mean Field Inference on Boltzman Machine
#
# TASK DESCRIPTION:
#
# Mean-Field Approximation is a useful method for inference, that originated in statistical physics.
# The Ising model in its two-dimensional form is diﬃcult to solve exactly, and therefore the mean-ﬁeld
# approximation methodology was utilized.
#
# This code will incorporate Mean ﬁeld inference for denoising binary images using MNIST dataset
#-----------------------------------------------------------------------------------
import numpy as np
import copy as cp
import os
import struct
import csv
from matplotlib import pyplot
import matplotlib as mpl


"""

i index belongs to the pixel we're making an update for.
j index belongs to the neighbors of the i pixel in the summations.

The order of update is important as implicitly mentioned in the text on page 263 
"After this blizzard of calculation, our inference algorithm is straightforward. 
We visit each hidden node in turn, set the associated pi_i to the value of the 
expression above assuming all the other pi_j are fixed at their current values, 
and repeat until convergence."  

The other q values are only fixed when updating a pixel, and this is a property of 
the mean-field approximation method. In other words, you do not fix the Q matrix, 
evaluate the whole matrix based on the old matrix, and then replace the whole matrix 
at once. You have to do the updates one pixel at a time.

"""

#as per instructions, use the first 20 images
N_IMAGES_TO_USE = 20
#minimal value which will be coded as 1. Lower values will be coded -1.
BINARY_THRES = 128
N_ITERATIONS = 10

MAX_ROW = 28
MAX_COL = 28
THETA_HH = 0.8
THETA_HX = 2.0

LITTLE_BIT = 1e-10

def read(path = "."):
    """
    Python function for importing the MNIST data set.  It returns an iterator
    of 2-tuples with the first element being the label and the second element
    being a numpy.uint8 2D array of pixel data for the given image.

    Expects uncompressed training images and labels files in directory "path"
    """

    fname_img = os.path.join(path, 'train-images-idx3-ubyte')
    fname_lbl = os.path.join(path, 'train-labels-idx1-ubyte')

    # Load everything in some numpy arrays
    with open(fname_lbl, 'rb') as flbl:
        magic, num = struct.unpack(">II", flbl.read(8))
        lbl = np.fromfile(flbl, dtype=np.int8)

    with open(fname_img, 'rb') as fimg:
        magic, num, rows, cols = struct.unpack(">IIII", fimg.read(16))
        img = np.fromfile(fimg, dtype=np.uint8).reshape(len(lbl), rows, cols)

    get_img = lambda idx: (lbl[idx], img[idx])

    # Create an iterator which returns each image in turn
    for i in range(min(N_IMAGES_TO_USE, len(lbl))):
        yield get_img(i)


def show(pixels):
    """
    Render a given numpy.uint8 2D array of pixel data.
    """
    fig = pyplot.figure()
    ax = fig.add_subplot(1,1,1)
    imgplot = ax.imshow(pixels, cmap=mpl.cm.Greys)
    imgplot.set_interpolation('nearest')
    ax.xaxis.set_ticks_position('top')
    ax.yaxis.set_ticks_position('left')

    pyplot.show()

    return fig


def binarize_image(images):
    b_data = []
    for image in images:
        #the following is an unsigned int array, create and int array for the -1s and 1s.
        pix = image[1]
        bin_image = np.zeros(pix.shape, dtype=int)
        bin_image[pix < BINARY_THRES] = -1
        bin_image[pix >= BINARY_THRES] = 1
        b_data.append(bin_image)
    return b_data


def read_noise_coords():
    #unzip the provided "SupplementaryAndSampleData.zip" into the working directory
    coords_by_image = []
    with open(os.path.join('SupplementaryAndSampleData', 'NoiseCoordinates.csv')) as infile:
        reader = csv.reader(infile)
        #skip header
        next(reader)
        for image in range(N_IMAGES_TO_USE):
            row_coords = next(reader)
            col_coords = next(reader)
            rc = [int(i) for i in row_coords[1:]]
            cc = [int(i) for i in col_coords[1:]]
            coords_by_image.append((rc, cc))
    return coords_by_image


def add_noise(images, noise_coords):
    noise_images = []
    for image, coords in zip(images, noise_coords):
        n_image = np.copy(image)
        for row_idx, col_idx in zip(coords[0], coords[1]):
            n_image[row_idx, col_idx] *= -1
        noise_images.append(n_image)
    return noise_images

def get_eight_neighbors(row, col):
    neighbors = []
    rowAbove = row - 1
    rowBelow = row + 1
    colRight = col + 1
    colLeft = col - 1

    for rc in range(rowAbove, rowBelow+1):
        if (rc >= 0) and (rc < MAX_ROW):
            for cc in range(colLeft, colRight+1):
                if (cc >= 0) and (cc < MAX_COL):
                    if not ((rc == row) and (cc == col)):
                        neighbors.append((rc, cc))
    return neighbors


def get_four_neighbors(row, col):
    neighbors = []
    rowAbove = row - 1
    rowBelow = row + 1
    colRight = col + 1
    colLeft = col - 1

    for rc in range(rowAbove, rowBelow+1):
        if (rc >= 0) and (rc < MAX_ROW) and not (rc == row):
            neighbors.append((rc, col))

    for cc in range(colLeft, colRight+1):
        if (cc >= 0) and (cc < MAX_COL) and not (cc == col):
            neighbors.append((row, cc))

    return neighbors

def read_update_order_coords():
    update_order_coords = []
    with open(os.path.join('SupplementaryAndSampleData', 'UpdateOrderCoordinates.csv')) as infile:
        reader = csv.reader(infile)
        #skip header
        next(reader)
        for image in range(N_IMAGES_TO_USE):
            row_coords = next(reader)
            col_coords = next(reader)
            rc = [int(i) for i in row_coords[1:]]
            cc = [int(i) for i in col_coords[1:]]
            update_order_coords.append((rc, cc))
    return update_order_coords

def read_initial_parameters():
    with open(os.path.join('SupplementaryAndSampleData', 'InitialParametersModel.csv')) as infile:
        initial_parameters = np.genfromtxt(infile, delimiter=',')
    return initial_parameters

def read_sample_denoised():
    with open(os.path.join('SupplementaryAndSampleData', 'SampleDenoised.csv')) as infile:
        sample_denoised = np.genfromtxt(infile, delimiter=',')
    return sample_denoised

def write_denoised_images(denoised_images):
    with open('Denoised.csv', 'w') as csvfile:
        """
        add lineterminator="\n" or it writes out in DOS format with CR LF
        """
        writer = csv.writer(csvfile, lineterminator="\n")
        for rowIndex in range(0, MAX_ROW):
            writer.writerow(denoised_images[rowIndex])

def write_energy_data(energies):
    with open("Energies.csv", 'w') as csvfile:

        """
        add lineterminator="\n" or it writes out in DOS format with CR LF
        """
        writer = csv.writer(csvfile, lineterminator="\n" )

        rows = np.asarray(energies)
        rows = rows.reshape((10,10))


        for imageIndex in range(0, 10):
            row = rows[imageIndex]
            row = ["%0.9f" % f for f in row]
            print(row)
            writer.writerow(row)

def calc_pi_numerator(i_row, i_col, Q, noisy_image):
    sum_term1 = 0.0
    sum_term2 = 0.0

    neighbors = get_four_neighbors(i_row, i_col)

    for j in neighbors:
        j_row = j[0]
        j_col = j[1]
        sum_term1 += (THETA_HH*(2*Q[j_row][j_col] - 1))

    sum_term2 += (THETA_HX * noisy_image[i_row][i_col])

    numerator = np.exp(sum_term1 + sum_term2)

    return numerator


def calc_pi_denominator_term2(i_row, i_col, Q, noisy_image):
    sum_term1 = 0.0
    sum_term2 = 0.0

    neighbors = get_four_neighbors(i_row, i_col)

    for j in neighbors:
        j_row = j[0]
        j_col = j[1]
        sum_term1 += -1*(THETA_HH*(2*Q[j_row][j_col] - 1))

    sum_term2 += -1*(THETA_HX * noisy_image[i_row][i_col])

    denominator_term2 = np.exp(sum_term1 + sum_term2)

    return denominator_term2

def calc_entropy_term(Q):
    entropy_term = 0.0

    for r in range(MAX_ROW):
        for c in range(MAX_COL):
            entropy_term += ((Q[r][c]*np.log(Q[r][c] + LITTLE_BIT)) + ((1-Q[r][c])*np.log(1-Q[r][c] + LITTLE_BIT)))
    return entropy_term


def calc_log_likelihood_term(Q, noisy_image):
    first_sum = 0.0
    second_sum = 0.0

    for i in range(MAX_ROW * MAX_COL):
        i_row = int(i/MAX_COL)
        i_col = int(i - i_row*MAX_COL)

        neighbors = get_four_neighbors(i_row, i_col)
        for j in neighbors:
            j_row = j[0]
            j_col = j[1]
            first_sum  += (THETA_HH * (2*Q[i_row][i_col] - 1) * (2*Q[j_row][j_col] - 1))

        """
        # interpretation of the math formulas such that the ONLY neighbor of i that is 
        # in X is the pixel X[i_row][i_col], so only ONE neighbor for the second sum equation
        """

        second_sum += (THETA_HX * (2*Q[i_row][i_col] - 1) * noisy_image[i_row][i_col])

    log_likelihood_term = first_sum + second_sum

    return log_likelihood_term

if __name__ == '__main__':
    t_data = list(read())

    bin_images = binarize_image(t_data)
    noise_images = add_noise(bin_images, read_noise_coords())

    update_order_coords = read_update_order_coords()


    """
    We want to construct a Q(H) that approximates the posterior for a Boltzmann machine. 
    
    We will choose Q(H) to have one factor for each hidden variable, 
    so Q(H) = q1(H1)q2(H2) . . . qN (HN )
    
    InitialParametersModel.csv. This file is the initial matrix Q stored as comma-separated 
    values, with a dimension of 28 × 28. Each entry of the matrix falls in the [0, 1] interval, 
    and the Qr,c entry denotes the qr,c[Hr,c = 1] initial probability. Here, a slight change 
    of notation happened with respect to what you have learned in the course; what you have 
    been denoting as qi[Hi = 1] through the course, is now named qr,c[Hr,c = 1] since we 
    have a hidden state for each pixel at the r row and c column index.

    Please note that the same initial parameters are used for all the Boltzman machines built 
    for each image.
    
    do this for image 0 first -- just fix for 0 and work until it matches image!
    """
    q_rc_initial = read_initial_parameters()

    energies = []

    denoised_images = []

    count = 0
    for image_index in range(10,20):
        """
        # do a deep copy of the initial q values, the initial values are the same for each image
        """
        Q = cp.deepcopy(q_rc_initial)

        """
        # get the order in which to step through the coordinates (pixels), this varies 
        # by image is specfied in the file UpdateOrderCoordinates.csv that we have already
        # read
        """
        coord_order = update_order_coords[image_index]

        for iteration in range(N_ITERATIONS):

            entropy_term = calc_entropy_term(Q)
            log_likelihood_term = calc_log_likelihood_term(Q, noise_images[image_index])

            E = entropy_term - log_likelihood_term
            energies.append(E)

            for pixel in range(MAX_ROW*MAX_COL):
                i_row = coord_order[0][pixel]
                i_col = coord_order[1][pixel]

                print('({},{}) = {}'.format(i_row,i_col,Q[i_row][i_col]))

                pi_numerator = calc_pi_numerator(i_row, i_col, Q, noise_images[image_index])
                pi_denominator_term1 = pi_numerator
                pi_denominator_term2 = calc_pi_denominator_term2(i_row, i_col, Q, noise_images[image_index])

                temp_q = pi_numerator / (pi_denominator_term1 + pi_denominator_term2)

                Q[i_row][i_col] = temp_q


        denoised_image = np.zeros(noise_images[image_index].shape, dtype=int)
        denoised_image[Q < 0.5] = 0
        denoised_image[Q >= 0.5] = 1

        if count == 0:
            denoised_images = cp.deepcopy(denoised_image)
        else:
            denoised_images = np.hstack((denoised_images, denoised_image))

        show(bin_images[image_index])
        show(noise_images[image_index])
        show(denoised_image)

        count += 1

    write_denoised_images(denoised_images)
    write_energy_data(energies)

    """ 
    # ROC CURVE
    """
    c_list = [5.0, 0.6, 0.4, 0.35, 0.3, 0.1]

    # the following choices illustrate ROC better
    #c_list = [5.0, 4.0, 3.0, 2.0, 1.0, 0.8, 0.6, 0.1]
    ROC_images = []
    TPRs = []
    FPRs = []

    for image_index in range(10, 20):
        """
        # do a deep copy of the initial q values, the initial values are the same for each image
        """
        for c_val in c_list:
            Q = cp.deepcopy(q_rc_initial)
            THETA_HH = c_val
            # idx = c_list.index(c_val)

            """
            # get the order in which to step through the coordinates (pixels), this varies 
            # by image is specfied in the file UpdateOrderCoordinates.csv that we have already
            # read
            """
            coord_order = update_order_coords[image_index]

            for iteration in range(N_ITERATIONS):
                for pixel in range(MAX_ROW * MAX_COL):
                    i_row = coord_order[0][pixel]
                    i_col = coord_order[1][pixel]

                    #print('({},{}) = {}'.format(i_row, i_col, Q[i_row][i_col]))

                    # -----------------------------------------------------------------------
                    # UPDATE Q[i_row][i_col] based on calculation of pi value in big formula
                    # -----------------------------------------------------------------------

                    pi_numerator = calc_pi_numerator(i_row, i_col, Q, noise_images[image_index])
                    pi_denominator_term1 = pi_numerator
                    pi_denominator_term2 = calc_pi_denominator_term2(i_row, i_col, Q, noise_images[image_index])

                    temp_q = pi_numerator / (pi_denominator_term1 + pi_denominator_term2)

                    Q[i_row][i_col] = temp_q

            denoised_image = np.zeros(noise_images[image_index].shape, dtype=int)

            denoised_image[Q < 0.5] = -1
            denoised_image[Q >= 0.5] = 1

            TP = 0.0
            FN = 0.0
            FP = 0.0
            TN = 0.0
            for row in range(MAX_ROW):
                for col in range(MAX_COL):
                    if denoised_image[row][col] != noise_images[image_index][row][col]:
                        if denoised_image[row][col] == bin_images[image_index][row][col]:
                            TP += 1 # true positive, "I flipped a bit and I should have"
                        else:
                            FP += 1 # false positive, "I flipped a bit and I shouldn't have"
                    else:
                        if denoised_image[row][col] != bin_images[image_index][row][col]:
                            FN += 1 # false negative, "I did nothing, but I should have flipped a bit"
                        else:
                            TN += 1 # true negative, "I did nothing, and I was supposed to do nothing"

            # from http://mlwiki.org/index.php/ROC_Analysis
            TPR = TP/(TP + FN)
            FPR = FP/(FP + TN)
            print('image: {} c: {} TP: {} FP: {} TN: {} FN: {} TPR: {} FPR: {}'.format(image_index, c_val, TP, FP, TN, FN, TPR, FPR))

            TPRs.append(TPR)
            FPRs.append(FPR)
        print('======================================')

    TPRs_arr = np.array(TPRs)
    TPRs_arr = np.reshape(TPRs_arr, (-1, len(c_list)))
    meanTPRs = np.mean(TPRs_arr, axis=0)

    FPRs_arr = np.array(FPRs)
    FPRs_arr = np.reshape(FPRs_arr, (-1, len(c_list)))
    meanFPRs = np.mean(FPRs_arr, axis=0)

    x = meanFPRs
    y = meanTPRs
    z = c_list

    fig, ax = pyplot.subplots()
    ax.plot(x, y, 'bo-')
    pyplot.xlabel("FPR",fontsize=16)
    pyplot.ylabel("TPR", fontsize=16)

    for X, Y, Z in zip(x, y, z):
        # Annotate the points 5 _points_ above and to the left of the vertex
        ax.annotate('{}'.format(Z), xy=(X, Y), xytext=(-5, 5), ha='right',
                    textcoords='offset points')

    pyplot.show()
