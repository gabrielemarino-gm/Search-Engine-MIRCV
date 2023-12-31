
# Search Engine User Manual

This application empowers you to perform various tasks related to information retrieval, encompassing the creation of an inverted index, making queries, and evaluating the performance of the retrieval model.

# Getting Started

- **Main Commands:**

  Use the following commands at the "Search Engine >" prompt:
    -   `createIndex`: Create the inverted index.
    -   `makeQuery`: Make a query.
    -   `evaluatePerformance`: Evaluate the performance of the model.
    -   `help`: Show available commands.
    -   `exit`: Exit the application.

## Creating the Inverted Index

To create the inverted index, use the command:

    createIndex -in <corpus_file> -ss -c -d

- Where:
    -   `-in <corpus_file>`: Specify the path of the corpus file (mandatory).
    -   `-ss`: Enable stopword removal and stemming.
    -   `-c`: Enable compression of the index.
    -   `-d`: Enable debug files creation.

## Query Handler

The Query Handler allows you to make queries interactively.
At first the system will ask the user for a configuration, which it will save and will not be asked again (unless the user uses the command s).

- While the Query Handler is active, the user can use the following command:
  -   Use `s` to set up your system and configure recovery options.
  -   Use `q` to exit the query manager.

####  Query Handler Setup

-   When prompted, choose between disjunctive and conjunctive mode.
-   Select the algorithm (DAAT or MaxScore) for the query.
-   Choose the scoring function (TF-IDF or BM25).
-   Set the value of k for the top-k documents.


## Evaluating Performance
To evaluate the performance of the model, use the command: `evaluatePerformance` This uses the <a href="https://github.com/usnistgov/trec_eval">Trec-Eval</a> tool.
Play Attention: You must have the Trec-Eval tool installed on your machine and the path to the tool must be specified in the config file if you want to use this command without using the `-in` option.
Use the following optional command:

    evaluatePerformance -in <trec_eval_local_path> -out <outpath_for_results> -y <2019 or 2020>

- Where:
    -   `-in <trec_eval_local_path>`: the path of the local Trec-Eval tool. DEFAULT: take the path from the config file.
    -   `-out <outpath_for_results>`: the path of the output file with the file txt to give in input to Trec-Eval tool. DEFAULT: data/trec-eval/resultsTrecEval.txt
    -   `-y`: choose the year of the Trec-Eval tool. 2020 or 2019. DEFAULT: 2020.

## Exiting the Application

-   To exit the application, use the command:
    
    `exit` 
