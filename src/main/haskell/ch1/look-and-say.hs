import Data.List
import Control.Monad

ant = iterate (group >=> sequence[length, head]) [1]
main = print (ant !! 10)