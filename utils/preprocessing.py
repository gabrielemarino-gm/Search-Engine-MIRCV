from typing import List, Tuple
import re
from nltk import PorterStemmer
import os
from icecream import ic

MAX_TERM_LENGTH = 64

class Preprocesser:

    def __init__(self, stemmstop: bool = False):

        # Initialize regular expression variables
        self.url_RGX = re.compile(r'(https?:\/\/\S+|www\.\S+)')
        self.html_exp = re.compile(r'<[^>]+>')
        self.non_digit_exp = re.compile(r'[^a-zA-Z ]') # TODO -> Controllare: Ha senso se ho fatto lower?
        self.multiple_space_exp = re.compile(r' +')
        self.consecutive_letters_exp = re.compile(r'(.)\\1{2,}')
        self.camel_case_exp = re.compile(r'(?<=[a-z])(?=[A-Z])') # TODO -> Controllare: Ha senso se ho fatto lower?

        # Initialize mode flags
        self.stemmstop_active = stemmstop

        if self.stemmstop_active:
            self.stemmer = PorterStemmer()

            stopwords_file_path = os.path.join(os.path.dirname(__file__), "..", "config", "stopwords.txt")
            with open(stopwords_file_path, 'r', encoding="utf-8") as f:
                self.stopwords = set(f.read().splitlines()) # Fucking Faster cit.

    # Generic text cleaning
    def clean(self, text):
        text = re.sub(self.url_RGX, '', text)
        text = re.sub(self.html_exp, ' ', text)
        text = re.sub(self.non_digit_exp, ' ', text)
        text = re.sub(self.multiple_space_exp, ' ', text)
        text = re.sub(self.consecutive_letters_exp, ' ' , text)
        text = re.sub(self.camel_case_exp, ' ', text)

        return text

    # Removal of stopwords from a list of words
    def remove_stopwords(self, tokens: List[str]) -> List[str]:
        filtered_tokens = [token for token in tokens if token not in self.stopwords]
        return filtered_tokens

    # Stemming of a list of words
    def perform_stemming(self, words):
        temp = []
        for i in range(words):
            temp.append(self.stemmer.stem(words[i]))

        return words

    def process(self, doc: str) -> Tuple[int, List[str]]:
        """
        Executes the preprocessing of a document
        :param doc: doc_id/tdoc_content
        :return: A tuple <docid, processed list of terms>
        """
        ic.disable()

        ic(doc)
        doc_id, text = ic(doc.split('\t'))

        ic.enable()

        # Text cleaning
        text = text.lower()
        text = self.clean(text)

        # Text tokenization
        terms = text.split(" ")

        # Stopwords removal
        if self.stopwords_active:
            terms = self.remove_stopwords(terms)

        # Stemming process
        if self.stemming_active:
            terms = self.perform_stemming(terms)

        return doc_id, terms
