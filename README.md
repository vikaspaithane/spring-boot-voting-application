# Voting System API

A Java 17 and Spring Boot demo for registering voters, managing candidates, and recording one vote per registered email. MySQL stores voter state and candidate totals. Run this demo locally with sample data. It uses email to identify a voter and does not verify who owns that email.

## Run locally

Start MySQL and create the database.

```sql
CREATE DATABASE IF NOT EXISTS crudOperation;
```

Open the `Vote Application` folder as a Maven project in IntelliJ. Set `DB_USERNAME` and `DB_PASSWORD` in the run configuration's environment variables. Set `DB_URL` if the database is on a different host, port, or schema.

Run `com.example.voting.VotingSystem`. You can also start it from the project folder with `./mvnw spring-boot:run` on Linux or macOS, or `mvnw.cmd spring-boot:run` on Windows. The API listens at `http://127.0.0.1:1313` by default.

If an old local database contains a password column from the previous version, the new code leaves that column unused. Use a fresh demo schema or remove the obsolete column after backing up anything you need.

## Try the API

1. Send `POST /api/users` with JSON such as `{"name":"Asha","email":"asha@example.com","phone":"9876543210"}`.
2. Send `POST /api/candidate` with `{"candidate":"Riya"}`.
3. Send `POST /api/candidate/vote?candidateName=Riya&email=asha%40example.com`.
4. Send `GET /api/candidate/Riya/votes` to check the tally. Repeating the vote returns `409 Conflict`.

Use `Content-Type: application/json` for the first two requests. Request DTOs accept voter details and candidate names. IDs, voter role, voter status, and vote counts are set by the server and appear in response DTOs. The vote endpoint reads query parameters.

| Method | Path | Action |
| --- | --- | --- |
| GET, POST | `/api/users` | List or register voters |
| GET, DELETE | `/api/users/{id}` | Read or remove a voter |
| GET | `/api/users/email/{email}` | Find a voter by email |
| GET, POST | `/api/candidate` | List or add candidates |
| GET, PUT, DELETE | `/api/candidate/{id}` | Read, rename, or remove a candidate |
| GET | `/api/candidate/name/{name}` | Find a candidate by name |
| GET | `/api/candidate/{name}/votes` | Read the vote count |
| POST | `/api/candidate/vote` | Record a vote |

The vote service updates voter status and increments the tally in one transaction. The status update only matches a voter who has not voted. The database increments the tally directly, so simultaneous votes do not overwrite one another. Users who have voted and candidates with recorded votes cannot be deleted through the API.

## Tests and debugging

Run `./mvnw test` or `mvnw.cmd test`. The tests use H2 and cover simultaneous votes, rollback when a candidate is missing, and server-managed voter and tally fields.

To follow a vote in IntelliJ, set a breakpoint in `VoteService.submitVote` and launch the app in Debug mode.
