### ClojureScript7
An implementation of the [7GUIs](https://eugenkiss.github.io/7guis/) in ClojureScript and Reagent by Jovan Jester. I took some liberties with the tasks as specified in the 7GUIs, mainly those where they suggested modal windows since I felt modal dialogs weren't really necessary in a web app context. 

### Live Demo
[![Netlify Status](https://api.netlify.com/api/v1/badges/41d6a16a-e804-49fc-ba8a-66eb2b2cae98/deploy-status)](https://app.netlify.com/sites/brave-haibt-e8be2e/deploys)
To view a live demo of this project go to https://clojurescript7.jester.cafe/

### Notes - Work In Progress

- What needs work
    - Mobile: originally I just wanted this to be a Clojure learning exercise so I designed for desktop browsers only, but now I've decided it would be nice to make it look good on mobile as well.
    - For Cells
        - Keyboard navigation and cell selection is a little buggy
        - Expand cell ranges into individual cell references (example ***A1:A4*** expands to ***A1 A2 A3 A4***) so that they can be fed into functions
        - Modify the expression parser so that is supports variadic functions (such as the standard spreadsheet functions like sum, average, etc.)
        - Purely numeric expressions work but change propagation when formulas use cell references is flaky. Needs a little more work.


