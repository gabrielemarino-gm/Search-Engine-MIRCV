
# Search Engine User Manual

This application allows you to perform various tasks related to information retrieval, including creating an inverted index, making queries, and evaluating the performance of the retrieval model.

# Getting Started

- **Main Commands:**
    
    -   Use the following commands at the "Search Engine >" prompt:
        -   `createIndex`: Create the inverted index.
        -   `makeQuery`: Make a query.
        -   `evaluatePerformance`: Evaluate the performance of the model.
        -   `help`: Show available commands.
        -   `exit`: Exit the application.

## Creating the Inverted Index

-   To create the inverted index, use the command:
    
    phpCopy code
    
    `createIndex -in <corpus_file> -ss -c -d` 
    
    -   `-in <corpus_file>`: Specify the path of the corpus file (mandatory).
    -   `-ss`: Enable stopword removal and stemming.
    -   `-c`: Enable compression of the index.
    -   `-d`: Enable debug files creation.

## Making Queries

-   To make a query, use the command:
    
    `makeQuery [s|q]` 
    
    -   `s`: Useful for setting up the system.
    -   `q`: Exit from the query handler mode.

## Evaluating Performance

-   To evaluate the performance of the model, use the command:
    
    `evaluatePerformance` 
    
    -   This uses the `trec_eval` tool.
    

## Query Handler

The Query Handler allows you to interactively make queries.
-   Use `s` to set up the system and configure retrieval options.
-   Use `q` to exit the Query Handler.

## System Setup

-   When prompted, choose between disjunctive and conjunctive mode.
-   Select the algorithm (DAAT or MaxScore) for the query.
-   Choose the scoring function (TF-IDF or BM25).
-   Set the value of k for the top-k documents.

## Exiting the Application

-   To exit the application, use the command:
    
    `exit` 
