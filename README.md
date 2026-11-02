# Online Quiz System in Java

A complete **Online Quiz System** written in pure Java for the console. Players register,
take timed multiple choice quizzes, get their answers marked automatically and can compare
themselves on the leaderboard. Administrators manage quizzes, questions and user accounts.

The project has **no external dependencies and no build tool**. It only needs a JDK, so it
can be cloned and started within seconds.

![Java](https://img.shields.io/badge/java-17%2B-orange)
![License](https://img.shields.io/badge/license-MIT-blue)
![Tests](https://img.shields.io/badge/tests-66%20passing-brightgreen)

---

## Features

### Player

| Feature | Description |
|---|---|
| Registration | Create an account with a username, name, email and password |
| Login | Passwords are stored as salted SHA-256 hashes, never as plain text |
| Timed quizzes | Every quiz has its own time limit with a live countdown |
| Question navigation | Skip a question, go back to the previous one or finish early |
| Review screen | All answers are listed before submitting, and any of them can be changed |
| Automatic marking | Answers are scored the moment the quiz is submitted |
| Answer sheet | After a quiz the correct answer of every question is shown |
| History | A list of all quizzes the player has taken, with pass or fail |
| Statistics | Number of quizzes, pass rate, average score and best score |
| Leaderboard | The best attempt of every player, ranked by percentage |

### Administrator

| Feature | Description |
|---|---|
| Dashboard | Number of users, quizzes, questions and attempts |
| Quiz management | Create, edit, activate, deactivate and delete quizzes |
| Question management | Add multiple choice and true/false questions, remove questions |
| User management | List accounts, deactivate them, change a role or reset a password |
| Attempt overview | Every attempt of every player |

---

## Quick start

You only need a **JDK 17 or newer** installed. Check with `java -version`.

### Windows

```bat
git clone https://github.com/<your-user>/online-quiz-system.git
cd online-quiz-system
run.bat
```

### Linux / macOS

```bash
git clone https://github.com/<your-user>/online-quiz-system.git
cd online-quiz-system
chmod +x run.sh
./run.sh
```

The first script compiles the project and then starts it. Afterwards `run.bat` / `run.sh`
start it straight away.

### Running it by hand

```bash
javac -encoding UTF-8 -d out src/com/quizsystem/*.java src/com/quizsystem/*/*.java
java -cp out com.quizsystem.Main
```

---

## Default accounts

The first start creates a default administrator and three sample quizzes:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Administrator |

Players register themselves from the main menu. Change the admin password from
**Administration console → Manage users → Reset password**.

---

## Running the tests

The test suite is part of the project and needs no test framework.

```bat
test.bat          :: Windows
```

```bash
./test.sh         # Linux / macOS
```

It prints every check and ends with a summary:

```
---------------------------------------------
Tests passed : 66
Tests failed : 0
---------------------------------------------
```

Each test runs against its own temporary data folder, so the tests never touch the data of
the real application.

---

## Project structure

```
online-quiz-system/
├── build.bat / build.sh        # compiles the sources into out/
├── run.bat / run.sh            # compiles (if needed) and starts the application
├── test.bat / test.sh          # compiles (if needed) and runs the test suite
├── data/                       # created at runtime, holds the saved data
└── src/com/quizsystem/
    ├── Main.java               # entry point
    ├── model/                  # the data classes
    │   ├── User, Quiz, Question, Option, Attempt, Answer
    │   └── Role, Difficulty, QuestionType
    ├── store/                  # reading and writing the data files
    │   ├── Storage             # helper for the flat text files
    │   ├── UserStore, QuizStore, QuestionStore, AttemptStore
    │   └── DataSeeder          # default admin and sample quizzes
    ├── service/                # the business rules
    │   ├── AuthService         # registration, login, passwords
    │   ├── QuizService         # quizzes and questions
    │   ├── AttemptService      # scoring an attempt
    │   ├── ResultService       # leaderboard and statistics
    │   ├── AdminService        # administration tasks
    │   └── ValidationException
    ├── util/
    │   ├── PasswordUtil        # salted SHA-256 hashing
    │   └── DateUtil            # timestamp formatting
    ├── ui/                     # everything the user sees
    │   ├── Console             # printing and reading
    │   ├── InputReader         # reads input on a background thread
    │   ├── QuizTimer           # the countdown
    │   ├── QuizRunner          # plays a quiz
    │   ├── LoginScreen, PlayerMenu, AdminMenu, App
    └── test/
        └── SelfTest            # the test suite
```

The code follows a simple layered structure:

```
ui  ->  service  ->  store  ->  model
```

The user interface never talks to the storage layer directly, and the business rules never
know anything about the console. That makes the rules easy to test and to reuse.

---

## How the data is stored

There is no database server to install. Everything is kept in plain text files inside the
`data` folder, one record per line and `|` between the fields:

```
data/
├── users.txt       id|username|full name|email|password hash|salt|role|active|created
├── quizzes.txt     id|title|description|category|time limit|pass mark|active|created by
├── questions.txt   id|quiz id|text|type|difficulty|marks|option 1..4|correct option
├── attempts.txt    id|quiz id|player|started|finished|score|total|passed|auto submitted
└── answers.txt     attempt id|question id|chosen option|correct|points
```

The data folder is created automatically on the first start. Deleting it resets the
application to a fresh installation.

The data folder can be moved somewhere else with the `quiz.data.dir` system property:

```bash
java -Dquiz.data.dir=/var/lib/quiz -cp out com.quizsystem.Main
```

---

## Security notes

- Passwords are never written to disk in plain text. Every account gets a random 16
  character salt and the password is stored as a salted SHA-256 hash.
- The login uses a constant time comparison, so it does not leak information through the
  time it takes to answer.
- The administrator can deactivate an account. A deactivated account can no longer log in.
- Free text that is written into a data file has the `|` character replaced, so a record can
  never break the file format.

---

## Ideas for further development

- A web front end with Spring Boot and Thymeleaf
- Question categories and difficulty based scoring
- A question pool that picks random questions for every attempt
- Export of the results as CSV or PDF
- Two factor authentication for the administrator

---

## License

Released under the [MIT License](LICENSE).
