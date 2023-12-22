package it.unipi;

import it.unipi.aide.CreateIndex;
import it.unipi.aide.MakeDataset;
import it.unipi.aide.ModelEvaluation;
import it.unipi.aide.QueryHandler;
import it.unipi.aide.utils.beautify.MemoryDisplay;

import java.util.Scanner;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class Driver
{
    // "\t"+YELLOW+"evaluatePerformance:"+ ANSI_RESET + "
    static Scanner scanner = new Scanner(System.in);
    static String commands = "\t"+YELLOW+"createIndex:"+ ANSI_RESET + "  create the inverted index from scratch." +
                             "\n\n" +
                             "\t"+YELLOW+"makeQuery:"+ ANSI_RESET + "  make a query. After the activation of the command, the application\n " +
                             "\t\t\t\twill ask for multiple input query, use also the following:\n" +
                             "\t\t\t[s]:   command useful for setup the system.\n" +
                             "\t\t\t[q]:   command useful for exit from the query handler mode.\n" +
                             "\n\n" +
                             "\t"+YELLOW+"evaluatePerformance:"+ ANSI_RESET + "  evaluate the performance of the model, using the trec_eval tool.\n\n" +
                             "\t"+YELLOW+"help:"+ ANSI_RESET + "  show the available commands\n\n" +
                             "\t"+YELLOW+"exit:"+ ANSI_RESET + "  exit from the application\n\n";
    public static void main(String[] args) throws Exception {
        MemoryDisplay memoryDisplay = new MemoryDisplay();

        System.out.println("\n" +
                BLUE + "███████ "+RED+"███████ "+ YELLOW + " █████  "+ BLUE +"██████  "+ GREEN +" ██████ "+ RED +"██   ██     "+ BLUE +"███████ "+ RED +"███    ██ "+ YELLOW +" ██████  "+ BLUE +"██ "+ GREEN +"███    ██ "+ BLUE +"███████"+ ANSI_RESET+"\n" +
                BLUE + "██      "+RED+"██      "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +"██      "+ RED +"██   ██     "+ BLUE +"██      "+ RED +"████   ██ "+ YELLOW +"██       "+ BLUE +"██ "+ GREEN +"████   ██ "+ BLUE +"██     "+ ANSI_RESET+"\n" +
                BLUE + "███████ "+RED+"█████   "+ YELLOW + "███████ "+ BLUE +"██████  "+ GREEN +"██      "+ RED +"███████     "+ BLUE +"█████   "+ RED +"██ ██  ██ "+ YELLOW +"██   ███ "+ BLUE +"██ "+ GREEN +"██ ██  ██ "+ BLUE +"█████  "+ ANSI_RESET+"\n" +
                BLUE + "     ██ "+RED+"██      "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +"██      "+ RED +"██   ██     "+ BLUE +"██      "+ RED +"██  ██ ██ "+ YELLOW +"██    ██ "+ BLUE +"██ "+ GREEN +"██  ██ ██ "+ BLUE +"██     "+ ANSI_RESET+"\n" +
                BLUE + "███████ "+RED+"███████ "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +" ██████ "+ RED +"██   ██     "+ BLUE +"███████ "+ RED +"██   ████ "+ YELLOW +" ██████  "+ BLUE +"██ "+ GREEN +"██   ████ "+ BLUE +"███████"+ ANSI_RESET+"\n" +
                "                                                                                                    \n\n" +
                "Welcome to the Search Engine!\n");
        System.out.println("Commands available:");
        System.out.println(commands);

        label:
        while (true)
        {
            System.out.print(BLUE + "Search Engine > " + ANSI_RESET);

            String option = scanner.nextLine();

            String[] splitCommands = option.split(" ");

            switch (splitCommands[0]) {
                case "createIndex":
                    CreateIndex.main(splitCommands);
                    System.out.println();
                    break;
                case "makeQuery":
                    QueryHandler.main(splitCommands, scanner);
                    System.out.println();
                    break;
                case "evaluatePerformance":
                    ModelEvaluation.main(splitCommands, scanner);
                    System.out.println();
                    break;
                case "makeDataset":
                    MakeDataset.main(splitCommands);
                    System.out.println();
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
                    break;
            }
        }

        memoryDisplay.end();
    }
}
