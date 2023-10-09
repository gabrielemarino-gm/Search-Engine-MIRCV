from typing import Dict, List


class Vocabulary:
    """
    Represents pairs <term, term frequency>
    """
    def __init__(self):
        self.vocab: Dict[str, int] = {}

    def add(self, term: str) -> None:
        if not self.vocab.get(term):
            # If an entry for that term doesn't exist, add one with value 1
            self.vocab[term] = 1
        else:
            # If it's present, increment it
            self.vocab[term] += 1

    def get(self, term: str) -> int:
        # Return frequency of given term
        return self.vocab.get(term)


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

    def increment(self) -> None:
        self.count += 1

    def __str__(self) -> str:
        return f'({self.doc_id}:{self.count})'


class InvertedIndex:
    """
    InvertedIndex structure
    Dict indexed by a term, each one containing a list of postings
    """
    def __init__(self):
        self.iidx: Dict[str, List[Posting]] = {}

    def add(self, doc: int, term: str) -> None:
        """
        :param term: Term to add to the vocabulary
        :param doc: Document containing that term
        :return: None
        """
        posting_list = self.iidx.get(term)
        if not posting_list:
            # First time we add that term
            new_posting = Posting(doc)
            self.iidx[term] = [new_posting]
        else:
            # Term already exists
            last_posting = posting_list[-1]
            if last_posting.get_doc_id() == doc:
                # Last posting regards current document
                last_posting.increment()
            else:
                # Last posting is not current document
                new_posting = Posting(doc)
                posting_list.append(new_posting)

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