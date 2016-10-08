#Data sorting and filtering

##Overview
This repository is my solution to [Mariner](http://marinerpartners.com/)'s programming task.

###Compilation and Execution Instructions
To compile and run, clone the repository, navigate to the repo root, and run
```
javac -cp lib/gson-2.7.jar:lib/opencsv-3.8.jar src/ReportFiltering.java
java -cp classes:lib/gson-2.7.jar:lib/opencsv-3.8.jar:src/ ReportFiltering
```

###Design Choices
There were a number of deliberate software design choices in this project.

Using the Gson library was desirable for the ability to set the values using reflection. This versatile and expedient feature allows for object instantiation almost immediately after class construction.

The OpenCSV library was also a matter of speed, as it allows the user to read in an entire file to a String array using only two lines of code. Setting values afterward in the object is trivial.

The XML parsing provided by Java is, by comparison, much more time consuming to define. It does, however, have the advantage of reading line-by-line, as opposed to reading the entire file into memory. Depending on the file size, this could be an extreme advantage.

In terms of time complexity of the tasks, my options were a relatively even playing field. I had originally considered using a PriorityQueue to store the reports, as this can be constructed in O(n) time, and would ensure that the reports were always in order. On second thought, though, I realized that this would necessitate O(nlogn) queries to poll the entire queue. My final approach was to construct the list in O(n) time using an ArrayList and sort the list using `Collections.sort()` before queries (O(nlogn), as sort() method uses Merge Sort). The rationale behind this was that insertions are (typically) more frequent than queries in log files, so fast insertion was the priority.

###Dependencies and Attributions
This software is written in Java, using Java SDK 1.8.

The external libraries employed are [Gson](https://github.com/google/gson) and [OpenCSV](http://opencsv.sourceforge.net/), both of which are provided and used under the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) license.

>###Original Readme
>
>Read the 3 input files reports.json, reports.csv, reports.xml and output a combined CSV file with the following >characteristics:
>
>- The same column order and formatting as reports.csv
>- All report records with packets-serviced equal to zero should be excluded
>- records should be sorted by request-time in ascending order
>
>Additionally, the application should print a summary showing the number of records in the output file associated >with each service-guid.
>
>Please provide source, documentation on how to run the program and an explanation on why you chose the tools/>libraries used.
>
>####Submission
>
>You may fork this repo, commit your work and let us know of your project's location, or you may email us your >project files in a zip file.
