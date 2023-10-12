package it.unipi;

import it.unipi.aide.CreateIndex;
import it.unipi.aide.QueryHandler;
import it.unipi.aide.ModelEvaluation;

public class Driver {
    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 0) {
            String option = args[0];
            String[] args2 = new String[0];

            if( args.length > 1 ){
                args2 =  new String[args.length-1];
                System.arraycopy(args, 1, args2, 0, args2.length);
            }

            if(option.equals("createIndex")) {
                new CreateIndex().main(args2);
                System.exit(0);
            }
            else if(option.equals("makeQuery")){
                new QueryHandler().main(args2);
                System.exit(0);
            }
            else if(option.equals("evaluatePerformance")){
                new ModelEvaluation().main(args2);
                System.exit(0);
            }
            else{
                System.err.println("Class Not Found");
                System.err.println("Available Classes:\n\tcreateIndex\n\tmakeQuery\n\tevaluatePerformance");
            }
        }
        System.err.println("No Input Given");
        System.err.println("Available Classes:\n\tcreateIndex\n\tmakeQuery\n\tevaluatePerformance");
    }
}
