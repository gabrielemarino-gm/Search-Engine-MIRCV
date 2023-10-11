import unittest
from structure import Vocabulary, InvertedIndex
from utils import Preprocesser


class ComponentsTesting(unittest.TestCase):
    def test_vocabulary(self):
        voc = Vocabulary()
        voc.add('Martina')
        voc.add('Matteo')
        voc.add('Martina')
        self.assertEqual(voc.get('Martina'), 2)
        self.assertEqual(voc.get('Matteo'), 1)
        voc.add('Martina')
        self.assertEqual(voc.get('Martina'), 3)

    def test_preprocessing(self):
        preproc = Preprocesser()
        pass

    def test_inverse_index(self):
        inverted_index = InvertedIndex()
        pass



if __name__ == '__main__':
    unittest.main()
