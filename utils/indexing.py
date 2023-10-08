import psutil
from icecream import ic
from structure import InvertedIndex, Vocabulary


class SPIMI:

    MAX_MEM = 80
    MAX_POSTINGLIST_SIZE = 1
    def __init__(self, ts: str, fp: str):
        ic("Init SPIMI")
        self.token_stream = self.parse_token_stream()
        self.output_path = fp

        self.dictionary = Vocabulary()
        self.inverted_index = InvertedIndex()

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

    def parse_token_stream(self) -> {}:
        pass

    def add_to_dictionaty(self, term: str) -> list:
        # Add to dictionary
        self.dictionary.add(term)
        # Add to the posting list
        self.inverted_index.add_term(term)
        return self.inverted_index.get_posting_list_by_term(term)
        pass

    def get_posting_list(self, term: str) -> list:
        # TODO
        pass

    def double_posting_list(self) -> list:
        # TODO
        pass

    def write_block_to_disk(self, sorted_terms: set()):
        # TODO
        pass

    def algorithm(self):
        """
        Implementation of the Single-Pass In-Memory Indexing
        :return:
        """
        memory = psutil.virtual_memory()

        i = 0
        token = set()
        while memory.percent <= self.MAX_MEM:
            token = self.token_stream[i]

            #if token is not self.dictionary:
            #    posting_list = self.add_to_dictionaty(token["term"])
            #else:
            #    posting_list = self.get_posting_list(token["term"])

            self.dictionary.add(token["term"])
            self.inverted_index.add(token["term"])

            if (len(posting_list) > self.MAX_POSTINGLIST_SIZE): #Da vedere meglio
                posting_list = self.get_posting_list()

            self.add_to_posting_list(posting_list, token["docid"])
            i += 1

        self.write_block_to_disk(sorted(self.dictionary))
        return self.output_path
