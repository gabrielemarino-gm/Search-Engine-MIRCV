from typing import Dict, List, Tuple


class Vocabulary:
    """
    Represents pairs <term, term frequency>
    """
    def __init__(self):
        self.vocab: Dict[str, int] = {}

    def add(self, term: str) -> None:
        if not self.vocab.get(term):
            self.vocab.update([(term, 1)])
        else:
            self.vocab.update([(term, self.vocab.get(term) + 1)])


class PostingList:
    """
    List of <docId, term frequency>
    """
    def __init__(self, document: int, freq: int = 1):
        self.postlist: Dict[int, Tuple(int, int)] = {}

    def increment_entry(self):
        self.tfd += 1
        pass


class InvertedIndex:
    """
    Represents pairs of <term, PostingList>
    """
    def __init__(self):
        self.iidx = Dict[str, List[PostingList]]

    def add(self, term: str, doc: int) -> None:
            """
            :param term: Term to add to the vocabulary
            :param doc: Document containing that term
            :return: None
            """
            
        pass

    def get_posting_list_by_term(self, term: str) -> Dict[str, List[PostingList]]:
        return self.iidx.get(term)



# InvertedIndex = {str, PostingList}
# PostingList = [(docid, termFrequency on that document)]

# iidx = Dict[          Lista di termini (si usa il Dict perche funge da hashMap)

#   str,                Str e' l'indicizzazione di iidx, quindi un termine
#   List[              Ogni termine ha una posting list (Coppie docid, frequency) TODO -> List o Dict?
#
#
#   ]
# ]
