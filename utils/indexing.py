import psutil
from icecream import ic
from structure import InvertedIndex, Vocabulary
from utils import Preprocesser
from typing import Dict


class SPIMI:

    def __init__(self, input_file: str, output_path: str, maxmem: int = 80, stemming: bool = False, stopword: bool = False, urls: bool = False):
        """
        Inizializza SPIMI
        Assegna varie variabili di configurazione
        """
        self.MAX_MEM = maxmem
        self.input_path = input_file
        self.output_path = output_path

        self.dictionary = Vocabulary()
        self.inverted_index = InvertedIndex()
        self.processor = Preprocesser(stemming=stemming, stopwords=stopword, delete_urls=urls)
        self.b = 0

        ic()
        print(f"SPIMI Parameters:\ninput_path: {self.input_path}\n output_path: {self.output_path}\n MAXMEM: {self.MAX_MEM}\n")

    def get_next_doc(self) -> Dict[str, int]:
        with open(self.input_path, 'r', encoding='utf-8') as f:
            for line in f:
                yield line

    def write_block_to_disk(self):
        # TODO Da rivedere meglio
        with open(f"Data/Block-{self.b}.txt", 'w') as f:
            f.write(str(self.inverted_index))

        with open(f"Data/Dictionary-{self.b}.txt", 'w') as f:
            f.write(str(self.dictionary))

        self.b += 1

    def algorithm(self) -> None:
        """
        Implementation of the Single-Pass In-Memory Indexing
        """

        id = 0

        memory = psutil.virtual_memory()
        for doc_content in self.get_next_doc():
            doc_id, terms = self.processor.process(doc_content)
            id += 1
            for t in terms:

                # Add the term to the dictionary. Overlapping handled by Dictionary
                if t == '':
                    continue

                self.dictionary.add(t)
                # Add the term to the inverted index
                self.inverted_index.add(t, doc_id)

            if memory.percent > self.MAX_MEM:
                print(f"Write block {self.b}")
                ic()
                # TODO -> Salva IndiceParziale su disco (anche Dizionario?)
                self.write_block_to_disk()
                del(self.dictionary)
                del(self.inverted_index)
                self.dictionary = Vocabulary()
                self.inverted_index = InvertedIndex()

            if id%100000 == 0:
                print(f"Document progression: {id}")
                ic()



# Pseudocode SPIMI (Single-Pass In-Memory Indexing)
# C'è una parte dell'algoritmo omessa: qualla che fa il parse dei documenti,
# e li trasforma nella coppia (Term, DocID) = token SPIMI va chiamato a loop per i vari token_strem
#
# SPIMI-INVERT(token_stream) Token = É una coppia Term-DocID

#     output file = NEWFILE()
#     dictionary = NEWHASH()
#     while (free memory available)
#     do token <— next(token_stream)
#         if term(token) NOT IN dictionary
#             then postings_list = ADDToDICTIONARY (dictionary, term(token))
#         else postings_list = GETPosTIngsLIsT(dictionary, term(token))
#         if full (postings_list)
#             then postings_list = DoUbLEPostINgsLIsT (dictionary, term(token))
#         ADDToPosTINgsLIsT (postings list, doclD(token))
#     sorted terms <— SORTTerMs(dictionary)
#     WRITE-BLOCKToDIsk(sorted_terms, dictionary, output file)
#     return output_file


# ----------------GLI APPUNTI DI MATTE----------------
# SPIMI -> Utilizza le classi InvertedIndex, Vucabulary e Preprocesser per applicare l'algoritmo

# Inizializzazione -> Prende in input il file da processare e in output dove mettere indici parziali/finali
# Una volta inizializzato tutto, la funzione algorithm() inizia a fare cose
# Gestione della memoria delegata a SPIMI

# Scrittura e lettura degli indici parziali/finali, la fa SPIMI o viene delegata alla classe InvertedIndex?
# Prima  o poi qualcuno dovra ciclare la lista di parole per ogni riga(documento), lo fa SPIMI