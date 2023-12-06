package it.unipi.aide;

public class MatteFaCose {

    public static void main(String[] argv) {

        String[] TERMINI = new String[] {"c", "d", "e", "f", "g", "h", "i", "j", "l", "m", "n", "o", "p", "q", "r", "s", "t"};
        String[] TERMINI2 = new String[] {"c", "d", "s", "t", "a", "b", "u", "v", "l"};

        for(String term : TERMINI2) {
            int WIN_LOWER_BOUND = 0;
            int WIN_UPPER_BOUND = TERMINI.length;
            int prev = -1;
            String ret = null;

            System.out.print(String.format("Searching for: %s\t", term));
            while (true) {
                int WIN_MIDDLE_POINT = (WIN_UPPER_BOUND - WIN_LOWER_BOUND) / 2 + WIN_LOWER_BOUND;
                if(prev == WIN_MIDDLE_POINT)
                    break;
                prev = WIN_MIDDLE_POINT;

//                System.out.println(String.format("[%d | %d | %d]", WIN_LOWER_BOUND, WIN_MIDDLE_POINT, WIN_UPPER_BOUND));

                if(WIN_UPPER_BOUND == WIN_LOWER_BOUND)
                    break;

                String middleTerm = TERMINI[WIN_MIDDLE_POINT];

                int comp = middleTerm.compareTo(term);

                if (comp == 0) {
                    ret = middleTerm;
                    break;
                } else if (comp > 0) {
                    WIN_UPPER_BOUND = WIN_MIDDLE_POINT;
                } else {
                    WIN_LOWER_BOUND = WIN_MIDDLE_POINT;
                }
            }
            if (ret != null)
                System.out.println(String.format("Found: %s!", ret));
            else
                System.out.println("Not found!");
        }
    }
}
