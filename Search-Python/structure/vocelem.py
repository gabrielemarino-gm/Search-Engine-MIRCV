from typing import Dict, List


class TermInfo:
    """
    Represents the information about a term:
        1) Frequency
        2) Offset of the file where the posting list of that term it's saved
    """
    def __init__(self, f: int = 1, o: int = 0):
        self.frequency = f
        self.offset = o

    def increment_frequency(self):
        self.frequency += 1

    def update_offset(self, o: int):
        self.offset = o

    def get_frequency(self):
        return self.frequency

    def get_offset(self):
        return self.offset


class Vocabulary:
    """
    Represents pairs <term, term_info>, the term_info contain frequency and
    the offset of the file where the posting list of that term it's saved
    """

    def __init__(self):
        self.vocab: Dict[str, TermInfo] = {}

    def add(self, term: str) -> None:
        if not self.vocab.get(term):
            # If an entry for that term doesn't exist, add one with value 1
            self.vocab[term] = TermInfo()
        else:
            # If it's present, increment it
            self.vocab[term].increment_frequency()

    def get(self, term: str) -> TermInfo:
        # Return frequency of given term
        return self.vocab.get(term)

    def get_term_info(self, term: str):
        return self.vocab[term]

    def get_list_of_terms(self):
        return self.vocab.keys()


class Posting:
    """
    One Posting
    Composed by a doc_id and term frequency on that document
    """

    def __init__(self, doc_id: int) -> None:
        self.doc_id = doc_id
        self.count = 1

    def get_doc_id(self) -> int:
        return self.doc_id

    def get_count(self) -> int:
        return self.count

    def increment(self) -> None:
        self.count += 1

    def __str__(self) -> str:
        return f'({self.doc_id}:{self.count})'


class PostingList:
    """
    The data structure representing an entire posting list for one term.
    Composed by the term and Posting
    """

    def __init__(self, t: str, pl: List[Posting]) -> None:
        self.term = t
        self.posting_list = pl

    def get_last_posting(self) -> Posting:
        return self.posting_list[-1]

    def __str__(self) -> str:
        ret = self.term + ": "
        for p in self.posting_list:
            ret += str(p)
        return ret


class InvertedIndex:
    """
    InvertedIndex structure
    Dict indexed by a term, each one containing a list of postings
    """

    def __init__(self):
        # TODO ATTENZIONE!!!!! Qui c'è una ridondanza da gestire,
        #  viene specificato due volte il termine per poter usare la comodità del vocabolario.
        self.iidx: [str, PostingList] = {}

    def add(self, doc: int, term: str) -> None:
        """
        :param term: Term to add to the inverted index
        :param doc: Document ID containing that term
        :return: None
        """
        posting_list = self.iidx.get(term)
        if not posting_list:
            # First time we add that term just create a new posting list
            self.iidx[term] = [PostingList(term, [Posting(doc)])]
        else:
            # Term already exists
            last_posting = posting_list.get_last_posting()

            if last_posting.get_doc_id() == doc:
                # Last posting regards current document
                last_posting.increment()
            else:
                # Last posting is not current document
                new_posting = Posting(doc)
                posting_list.append(new_posting)

            self.iidx[term] = PostingList(term, posting_list)

    def get_posting_list_by_term(self, term: str) -> List[Posting]:
        return self.iidx.get(term)

    def __str__(self) -> str:
        r = f'['
        for key in self.iidx:
            r += f'\'{key}\':'
            for e in self.iidx.get(key):
                r += f'{e}'
            r += ','
        r += f']'
        return r
