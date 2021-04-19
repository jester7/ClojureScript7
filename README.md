### ClojureScript7
An implementation of the [7GUIs](https://eugenkiss.github.io/7guis/) in ClojureScript and Reagent by Jovan Jester. I took some liberties with the tasks as specified in the 7GUIs, mainly those where they suggested modal windows since I felt modal dialogs weren't really necessary in a web app context. 

### Live Demo
To view a live demo of this project go to https://clojurescript7.jester.cafe/

### Notes - Work In Progress

- What's currently working
    - All the components are more or less working fully except for the last one, Cells (a future Excel killer)
    - For Cells
        - Evaluation of infix expressions (numbers and simple cell references like ***A1***)
        - Basic keyboard navigation to move across cells
- What needs work
    - Originally I just wanted this to be a Clojure learning exercise so I designed for desktop browsers only, but now I've decided it would be nice to make it look good on mobile as well.
    - For Cells
        - Expand cell ranges into individual cell references (example ***A1:A4*** expands to ***A1 A2 A3 A4***) so that they can be fed into functions
        - Modify the expression parser so that is supports variadic functions (such as the standard spreadsheet functions like sum, average, etc.)
        - Need to think about how to handle change propagation when cell values change

### Deploy Status
[![Netlify Status](https://api.netlify.com/api/v1/badges/41d6a16a-e804-49fc-ba8a-66eb2b2cae98/deploy-status)](https://app.netlify.com/sites/brave-haibt-e8be2e/deploys)

