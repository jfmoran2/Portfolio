import fileinput
from collections import Counter, defaultdict
import csv

is_debug_mode = True
laplace_factor = 0.1
legs_values = [0, 2, 4, 5, 6, 8]
master_class_list = [1, 2, 3, 4, 5, 6, 7]
legs_index = 12
n_features = 16


def get_input():
    # parse command line
    if is_debug_mode:
        # return (load_csv("sample_data1.txt"))
        return(load_csv('zoo2.data'))
    else:
        return (load_stdin())


def load_stdin():
    input_data = []

    linecount = 0
    for line in fileinput.input():

        # skip header
        if linecount > 0:
            new_list = [str(elem) for elem in line.rstrip('\n').split(',')]
            input_data.append(new_list)

        linecount = linecount + 1
    return input_data


def load_csv(filename):
    lines = csv.reader(open(filename, 'r'))

    input_data = list(lines)

    # skip header
    input_data = input_data[1:]

    return input_data


# def calc_class_probabilities(training_labels, training_labels_2):
def calc_class_probabilities(training_labels_2):
    # probability for any class 1-7, even if none in training

    class_probabilities = dict.fromkeys(master_class_list, 0.0)
    class_counts = dict.fromkeys(master_class_list, 0.0)

    for i in training_labels_2:
        class_probabilities[i] = class_probabilities.get(i, 0) + 1

    for i in training_labels_2:
        class_counts[i] = class_probabilities[i]

    temp_set_2 = set(training_labels_2)
    unique_labels_2 = list(temp_set_2)
    n_unique_labels_2 = len(unique_labels_2)

    for key in class_probabilities.keys():
        class_probabilities[key] = (class_probabilities[key] + laplace_factor) / (float(n_unique_labels_2)
                                                                                  + laplace_factor * len(
                    master_class_list))
    return class_counts, class_probabilities


def calc_probabilities(probabilities, class_counts):
    for class_index in master_class_list:
        for feature_index in range(n_features):
            feature_list = probabilities[class_index][feature_index]
            for value in feature_list:
                unique_features = len(feature_list)
                n_samples_class = class_counts[class_index]

                n_values = feature_list[value]

                probability = (n_values + laplace_factor) / (float(n_samples_class) + (laplace_factor*unique_features))
                probabilities[class_index][feature_index][value] = probability

    return probabilities


def calculate_naive_bayes(training_data, training_labels, testing_row):

    temp_set = set(training_labels)
    classes = list(temp_set)
    number_of_features = len(training_data[0])

    class_counts, class_probabilities = calc_class_probabilities(training_labels)

    probabilities = {}

    for unique_class in master_class_list:
        probabilities[unique_class] = defaultdict(list)

    # create full probabilities dictionary with 0.0 in every spot as a place holder
    for unique_class in master_class_list:
        for feature in range(number_of_features):
            if feature != legs_index:
                features = dict.fromkeys([1, 0], 0.0)
            else:
                features = dict.fromkeys(legs_values, 0.0)

            probabilities[unique_class][feature] = features

    for label in classes:

        row_indices = []
        for idx1 in range(0, len(training_labels)):
            if training_labels[idx1] == label:
                row_indices.append(idx1)

        data_rows_this_label = [training_data[row_idx] for row_idx in row_indices]
        n_cols = len(data_rows_this_label[0])

        for feature_index in range(0, n_cols):

            feature_column = []
            for aRow in data_rows_this_label:
                feature_column.append(aRow[feature_index])

            counts = Counter(feature_column)

            for count in counts:
                probabilities[label][feature_index][count] = counts[count]

    # calculate the laplace smoothed probabilities
    probabilities = calc_probabilities(probabilities, class_counts)

    temp_results = {}
    for class_label in classes:
        class_probability = class_probabilities[class_label]

        for test_col in range(len(testing_row)):
            prob_values = probabilities[class_label][test_col]

            class_probability *= prob_values[testing_row[test_col]]

        temp_results[class_label] = class_probability

    return temp_results


if __name__ == '__main__':
    input_data = get_input()

    class_labels = []
    for i in range(len(input_data)):
         class_labels.append(input_data[i][17])

    class_labels = [int(i) for i in class_labels]

    no_animal_data = []
    for i in range(len(input_data)):
        row = []
        for j in range(1, 18):
            row.append(input_data[i][j])
        no_animal_data.append(row)

    no_animal_data = [list(map(int, i)) for i in no_animal_data]

    training_row_indices = []
    testing_row_indices = []
    for i in range(0, len(class_labels)):
        if class_labels[i] == -1:
            testing_row_indices.append(i)
        else:
            training_row_indices.append(i)

    training_row_indices = [int(i) for i in training_row_indices]
    testing_row_indices = [int(i) for i in testing_row_indices]

    training_data = [no_animal_data[i] for i in training_row_indices]
    training_labels = [class_labels[i] for i in training_row_indices]

    temp_test_data = [no_animal_data[i] for i in testing_row_indices]

    testing_data = []
    for i in range(len(temp_test_data)):
        row = []
        for j in range(0, 16):
            row.append(temp_test_data[i][j])
        testing_data.append(row)

    for testing_row in testing_data:
        results = calculate_naive_bayes(training_data, training_labels, testing_row)
        predicted_class = max(results, key=results.get)
        print(str(predicted_class))
