package it.unipi;

import it.unipi.aide.CreateIndex;
import it.unipi.aide.ModelEvaluation;
import it.unipi.aide.QueryHandler;
import it.unipi.aide.utils.beautify.MemoryDisplay;

import java.util.Scanner;

import static it.unipi.aide.utils.ColorText.*;

public class Driver
{
    // "\t"+YELLOW+"evaluatePerformance:"+ ANSI_RESET + "
    static Scanner scanner = new Scanner(System.in);
    static String commands = "\t"+YELLOW+"createIndex:"+ ANSI_RESET + "  command useful for create the inverted index from scratch. The comands available are:\n" +
                             "\t\t\t[-in]:   MANDATORY, indicate the input path, where the corpus is place\n" +
                             "\t\t\t[-ss]:   OPTIONAL, indicate if the stopword and stemming should be applied\n" +
                             "\t\t\t[-d]:    OPTIONAL, indicate if the debug mode should be activated\n" +
                             "\t\t\t[-c]:    OPTIONAL, indicate if the compression should be applied\n\n" +  // TODO: Forse è meglio metterela nel file di configurazione
                             "\t"+YELLOW+"makeQuery:"+ ANSI_RESET + "  command useful for make a query. After the activation of the command, the application\n " +
                             "\t\t\t\twill ask for multiple input query, use also the following:\n" +
                             "\t\t\t[s]:   command useful for setup the system.\n" +
                             "\t\t\t[q]:   command useful for exit from the query handler mode.\n" +
                             "\n\n" +
                             "\t"+YELLOW+"evaluatePerformance:"+ ANSI_RESET + "  command useful for evaluate the performance of the model, using the trec_eval tool.\n" +
                             "\t\t\t[-in]:   MANDATORY, indicate the input path, where the corpus is place\n" +
                             "\t\t\t[-y]:    OPTIONAL, indicate the year of the trec_eval tool to use, version available: 2019, 2020\n\n" +
                             "\t"+YELLOW+"help:"+ ANSI_RESET + "  command useful for show the available commands\n\n" +
                             "\t"+YELLOW+"exit:"+ ANSI_RESET + "  command useful for exit from the application\n\n";
    public static void main(String[] args) throws Exception
    {
        MemoryDisplay memoryDisplay = new MemoryDisplay();

        System.out.println("\n" +
                BLUE + "███████ "+RED+"███████ "+ YELLOW + " █████  "+ BLUE +"██████  "+ GREEN +" ██████ "+ RED +"██   ██     "+ BLUE +"███████ "+ RED +"███    ██ "+ YELLOW +" ██████  "+ BLUE +"██ "+ GREEN +"███    ██ "+ BLUE +" ███████"+ ANSI_RESET+"\n" +
                BLUE + "██      "+RED+"██      "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +"██      "+ RED +"██   ██     "+ BLUE +"██      "+ RED +"████   ██ "+ YELLOW +"██       "+ BLUE +"██ "+ GREEN +"████   ██ "+ BLUE +" ██     "+ ANSI_RESET+"\n" +
                BLUE + "███████ "+RED+"█████   "+ YELLOW + "███████ "+ BLUE +"██████  "+ GREEN +"██      "+ RED +"███████     "+ BLUE +"█████   "+ RED +"██ ██  ██ "+ YELLOW +"██   ███ "+ BLUE +"██ "+ GREEN +"██ ██  ██ "+ BLUE +" █████  "+ ANSI_RESET+"\n" +
                BLUE + "     ██ "+RED+"██      "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +"██      "+ RED +"██   ██     "+ BLUE +"██      "+ RED +"██  ██ ██ "+ YELLOW +"██    ██ "+ BLUE +"██ "+ GREEN +"██  ██ ██ "+ BLUE +" ██     "+ ANSI_RESET+"\n" +
                BLUE + "███████ "+RED+"███████ "+ YELLOW + "██   ██ "+ BLUE +"██   ██ "+ GREEN +" ██████ "+ RED +"██   ██     "+ BLUE +"███████ "+ RED +"██   ████ "+ YELLOW +" ██████  "+ BLUE +"██ "+ GREEN +"██   ████ "+ BLUE +" ███████"+ ANSI_RESET+"\n" +
                "                                                                                                    \n\n" +
                "Welcome to the Search Engine!\n");
        System.out.println("Commands available:");
        System.out.println(commands);

        while (true)
        {
            System.out.print(BLUE + "Search Engine > " + ANSI_RESET);
            String option = scanner.nextLine();
            // if(args.length > 1 )
            // {
            //     args2 =  new String[args.length-1];
            //     System.arraycopy(args, 1, args2, 0, args2.length);
            // }
            
            String[] splitCommands = option.split(" ");


            if (splitCommands[0].equals("createIndex"))
            {
                new CreateIndex().main(splitCommands);
                System.out.println();
            }
            else if(splitCommands[0].equals("makeQuery"))
            {
                new QueryHandler().main(splitCommands);
                System.out.println();
            }
            else if(splitCommands[0].equals("evaluatePerformance"))
            {
                new ModelEvaluation().main(splitCommands);
                System.out.println();
            }
            else if (splitCommands[0].equals("help"))
            {
                System.out.printf(commands);
            }
            else if (splitCommands[0].equals("exit"))
            {
                System.out.println("Exiting...");
                break;
            }
            else
            {
                System.out.println(RED + "Search Engine ERR > Command Not Found. Try one of the following:" + ANSI_RESET);
                System.out.println(commands);
            }

        }
        memoryDisplay.end();
    }
}
