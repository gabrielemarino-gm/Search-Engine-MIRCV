from typing import List, Tuple, Set
import re
from icecream import ic
from nltk import PorterStemmer
import utils.constant

class Preprocesser:

    def __init__(self, stemming: bool = False, stopwords: bool = False, urls: bool = False):

        # Initialize regular expression variables
        self.url_RGX = re.compile(r'[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)')
        self.html_exp = re.compile(r'<[^>]+>')
        self.non_digit_exp = re.compile(r'[^a-zA-Z ]')
        self.multiple_space_exp = re.compile(r' +')
        self.consecutive_letters_exp = re.compile(r'(.)\\1{2,}')
        self.camel_case_exp = re.compile(r'(?<=[a-z])(?=[A-Z])')

        # Initialize mode flags
        self.stemming_active = stemming
        self.stopwords_active = stopwords
        self.urls_check_active = urls

    # Application of regular expression for a first cleaning operation
    def clean(self, text):

        if self.urls_check_active == True:
            re.sub(self.url_RGX, '', text)

        re.sub(self.html_exp, '', text)
        re.sub(self.non_digit_exp, '', text)
        re.sub(self.multiple_space_exp, '', text)
        re.sub(self.consecutive_letters_exp, '', text)
        re.sub(self.camel_case_exp, '', text)

        return text

    # Removal of stopwords from a list of words
    def remove_stopwords(self, tokens: List[str], stopwords: Set[str]) -> List[str]:
        with open('pg345.txt', 'r', encoding="utf-8") as f:
            stopwords_file_text = f.read()
            stopwords = stopwords_file_text.splitlines()

            for word in stopwords:
                if word in tokens:
                    tokens.remove(word)
            return tokens

    # Stemming of a list of words
    def perform_stemming(self, words):
        stemmer = PorterStemmer()

        for word in words:
            index = words.index(words)
            word[index] = stemmer.stem(word)

        return words

    def process(self, doc: str) -> Tuple[int, List[str]]:
        """
        Get as input a line <docid/ttext>
        return DocID and the list of terms preprocessed
        """
        docId, text = doc.split('/t')

        # Text cleaning
        text = self.clean(text)

        # Text tokenization
        terms = text.split(" ")

        if(self.stopwords_active == True):
            terms = self.remove_stopwords(terms)

        if(self.stemming_check == True):
            terms = self.perform_stemming(terms)

        return terms

