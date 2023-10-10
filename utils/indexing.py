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

        ic(f"SPIMI Parameters:\ninput_path: {self.input_path}\n output_path: {self.output_path}\n MAXMEM: {self.MAX_MEM}\n")

    def get_next_doc(self) -> str:
        with open(self.input_path, 'r', encoding='utf-8') as f:
            for line in f:
                yield line

    def write_block_to_disk(self):
        # TODO Da rivedere meglio
        with open(f"{self.output_path}/Block-{self.b}.txt", 'w') as f:
            f.write(str(self.inverted_index))

        with open(f"{self.output_path}/Dictionary-{self.b}.txt", 'w') as f:
            f.write(str(self.dictionary))

        self.b += 1

    def algorithm(self) -> None:
        """
        Implementation of the Single-Pass In-Memory Indexing
        """
        id = 0

        for doc_content in self.get_next_doc():
            doc_id, terms = self.processor.process(doc_content)
            id += 1
            for t in terms:

                # Add the term to the dictionary. Overlapping handled by Dictionary
                if t == '':
                    continue

                self.dictionary.add(t)
                # Add the term to the inverted index
                self.inverted_index.add(doc_id, t)

            if psutil.virtual_memory().percent > self.MAX_MEM:
                ic(f"Write block {self.b}")
                # TODO -> Salva IndiceParziale su disco (anche Dizionario?)
                self.write_block_to_disk()

                del self.dictionary
                del self.inverted_index
                self.dictionary = Vocabulary()
                self.inverted_index = InvertedIndex()

            if id % 100000 == 0:
                ic(f"Document progression: {id}")
