SalaryPredictor
===============

Uses Weka to predict salary for the given data.

Building
========

This can be built using gradle. The command is

```cmd
gradle buildAll
```

This command builds 2 jar files namely DataCleaner.jar and TestDataFileCreator.jar

DataCleaner.jar
---------------

This tool accepts the train_features and train_salary files and generates a training data file in .arff format as accepted by WEKA. It also cleans up the data by removing outliers in the salary.

TestDataFileCreator.jar
-----------------------

This tool acceptes the test_features file and converts it to an ARFF fille as accepted by WEKA.

WEKA model
----------

The trained model file is available in the model directory. For this exercise, we trained using linear regression.

Prediction on test data
-----------------------

Prediction on test data is available in the data directory. The file name is jobid_salary.csv


