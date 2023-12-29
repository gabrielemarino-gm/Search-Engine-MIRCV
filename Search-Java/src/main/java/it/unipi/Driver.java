package it.unipi;

import it.unipi.aide.CreateIndex;
import it.unipi.aide.MakeDataset;
import it.unipi.aide.ModelEvaluation;
import it.unipi.aide.QueryHandler;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.beautify.MemoryDisplay;

import java.util.Scanner;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class Driver
{
    // "\t"+YELLOW+"evaluatePerformance:"+ ANSI_RESET + "
    static Scanner scanner = new Scanner(System.in);
    static String commands = "\t"+YELLOW+"createIndex:"+ ANSI_RESET + "  create the inverted index from scratch. The command that must and could be used are:\n" +
                                "\t\t\t-in <corpus_file>:   the path of the corpus file. MANDATORY.\n" +
                                "\t\t\t-ss:                 enable the stopword removal and the stemming.\n" +
                                "\t\t\t-c:                  enable the compression of the index.\n" +
                                "\t\t\t-d:                  enable the debug files creation.\n\n" +

                             "\t"+YELLOW+"makeQuery:"+ ANSI_RESET + "  make a query. After the activation of the command, the application will ask for setting up the system.\n " +
                             "\t\t\tThe command could be used are while the query handler is active are:\n" +
                             "\t\t\t[s]:   command useful for setup the system.\n" +
                             "\t\t\t[q]:   command useful for exit from the query handler mode.\n\n" +

                             "\t"+YELLOW+"evaluatePerformance:"+ ANSI_RESET + "  evaluate the performance of the model, using the trec_eval tool.\n" +
                             "\t\t\t-in <treceval_path>:   the path of the local Trec-Eval tool. The tool need to be already compiled. DEFAULT: take the path from the config file.\n" +
                             "\t\t\t-out <output_path>:    the path of the output file with the file txt to give in input to Trec-Eval tool. DEFAULT: data/trec-eval/resultsTrecEval.txt\n" +
                             "\t\t\t-y :                   choose the year of the Trec-Eval tool. 2020 or 2019. DEFAULT: 2020.\n\n" +
                             "\t"+YELLOW+"help:"+ ANSI_RESET + "  show the available commands\n\n" +
                             "\t"+YELLOW+"exit:"+ ANSI_RESET + "  exit from the application\n\n";
    public static void main(String[] args) throws Exception
    {
        MemoryDisplay memoryDisplay = new MemoryDisplay();

        System.out.println("\n" +
                BLUE + "███████ "+RED+"███████ "+ YELLOW + " █████  "+ BLUE +"██████  "+ GREEN +" ██████ "+ RED +"██   ██     "+ BLUE +"███████ "+ RED +"███    ██ "+ YELLOW +" ██████  "+ BLUE +"██ "+ GREEN +"███    ██ "+ BLUE +"███████"+ ANSI_RESET+"\n" +
                BLUE + "██      "+RED+"██      "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +"██      "+ RED +"██   ██     "+ BLUE +"██      "+ RED +"████   ██ "+ YELLOW +"██       "+ BLUE +"██ "+ GREEN +"████   ██ "+ BLUE +"██     "+ ANSI_RESET+"\n" +
                BLUE + "███████ "+RED+"█████   "+ YELLOW + "███████ "+ BLUE +"██████  "+ GREEN +"██      "+ RED +"███████     "+ BLUE +"█████   "+ RED +"██ ██  ██ "+ YELLOW +"██   ███ "+ BLUE +"██ "+ GREEN +"██ ██  ██ "+ BLUE +"█████  "+ ANSI_RESET+"\n" +
                BLUE + "     ██ "+RED+"██      "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +"██      "+ RED +"██   ██     "+ BLUE +"██      "+ RED +"██  ██ ██ "+ YELLOW +"██    ██ "+ BLUE +"██ "+ GREEN +"██  ██ ██ "+ BLUE +"██     "+ ANSI_RESET+"\n" +
                BLUE + "███████ "+RED+"███████ "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +" ██████ "+ RED +"██   ██     "+ BLUE +"███████ "+ RED +"██   ████ "+ YELLOW +" ██████  "+ BLUE +"██ "+ GREEN +"██   ████ "+ BLUE +"███████"+ ANSI_RESET+"\n" +
                "                                                                                                    \n\n" +
                "Welcome to the Search Engine! For help digit help\n");

        // Create config file if it doesn't exist
        ConfigReader.createConfigFile();

        label:
        while (true)
        {
            System.out.print(BLUE + "Search Engine > " + ANSI_RESET);
            scanner = new Scanner(System.in);
            String option = scanner.nextLine();
            String[] splitCommands = option.split(" ");

            switch (splitCommands[0])
            {
                case "createIndex":
                    CreateIndex.main(splitCommands);
                    System.exit(0);
                    break;
                case "makeQuery":
                    QueryHandler.main(splitCommands, scanner);
                    break;
                case "evaluatePerformance":
                    ModelEvaluation.main(splitCommands, scanner);
                    System.exit(0);
                    break;
                case "help":
                    System.out.printf(commands);
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    break label;
                default:
                    System.out.println(RED + "Search Engine ERR > Command Not Found. Try one of the following:" + ANSI_RESET);
                    System.out.println(commands);
            }
        }

        memoryDisplay.end();
    }
}
