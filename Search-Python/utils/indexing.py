import psutil
from icecream import ic
from structure import InvertedIndex, Vocabulary
from structure.vocelem import TermInfo
from utils import Preprocesser
from typing import Dict
import pickle


class SPIMI:

    def __init__(self, input_file: str, output_path: str, maxmem: int = 80, stemmstop: bool = False):
        """
        Inizializza SPIMI
        Assegna varie variabili di configurazione
        """
        self.MAX_MEM = maxmem
        self.input_path = input_file
        self.output_path = output_path

        self.dictionary = Vocabulary()
        self.inverted_index = InvertedIndex()
        self.processor = Preprocesser(stemmstop=stemmstop)  # , stopwords=stopword, delete_urls=urls)
        self.b = 0
        self.offsets_dictionary = {}

        ic(f"SPIMI Parameters:\ninput_path: {self.input_path}\n output_path: {self.output_path}\n MAXMEM: {self.MAX_MEM}\n")

    def get_next_doc(self) -> str:
        with open(self.input_path, 'r', encoding='utf-8') as f:
            for line in f:
                yield line

    def write_block_to_disk(self) -> bool:
        """
        Save in a binary file all the posting list found up to now
        :return: a bool indicates success of the procedure
        """
        try:

            with open(f"{self.output_path}/block/Block-{self.b}.bin", "wb") as file_bin:
                for t in self.dictionary.get_list_of_terms():
                    for pl in self.inverted_index.get_posting_list_by_term(t):
                        pickle.dump(pl, file_bin)
                        offset = file_bin.tell()
                        # For the moment I'll save the offset in the term info saved in the vocabulary.
                        self.dictionary.get_term_info(t).update_offset(offset)

            with open(f"{self.output_path}/dictionary/Dictionary-{self.b}.bin", "wb") as file_bin:
                pickle.dump(self.dictionary, file_bin)
        except:
            return False

        return True

    def write_block_to_disk_debug(self):
        with open(f"{self.output_path}/debug/Block-{self.b}.txt", 'w') as f:
            f.write(str(self.inverted_index))

        with open(f"{self.output_path}/debug/Dictionary-{self.b}.txt", 'w') as f:
            f.write(str(self.dictionary))

    def algorithm(self, debug: bool = False) -> None:
        """
        Implementation of the Single-Pass In-Memory Indexing
        """

        ic("Start algorithm")
        id = 0

        for doc_content in self.get_next_doc():
            doc_id, text = ic(doc_content.split('\t'))
            terms = self.processor.process(text)
            id += 1
            for t in terms:
                # Add the term to the dictionary. Overlapping handled by Dictionary
                if t == '':
                    continue

                self.dictionary.add(TermInfo(t))

                # Add the term to the inverted index
                self.inverted_index.add(doc_id, t)

            # Check the main memory, if it's over the threshold write the synopsis of the block
            if psutil.virtual_memory().percent > self.MAX_MEM:
                ic(f"Write block {self.b}")
                # TODO -> Salva IndiceParziale su disco (anche Dizionario? YEP)

                if debug:
                    self.write_block_to_disk_debug()

                if self.write_block_to_disk():
                    self.b += 1
                    del self.dictionary
                    del self.inverted_index
                    self.dictionary = Vocabulary()
                    self.inverted_index = InvertedIndex()
                else:
                    print("ERROR: Not able to write the binary file")
                    break

                if id % 100000 == 0:
                    ic(f"Document progression: {id}")
