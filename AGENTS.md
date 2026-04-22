# AGENTS.md

## Quick Start

```bash
# Recommended: PowerShell manual startup
# Terminal 1 - Backend (port 9090)
Set-Location E:\vscode\code\pcc2\backend
$env:DB_PASSWORD = "hp197027"
mvn spring-boot:run

# Terminal 2 - Frontend (port 5173)
Set-Location E:\vscode\code\pcc2\frontend
npm run dev

# Optional: One-click (cmd)
E:\vscode\code\pcc2\start_project.bat
```

- **Backend**: http://localhost:9090
- **Frontend**: http://localhost:5173
- **Test account**: `aaa` / `aaa12345`

## Project Structure

| Directory | Description |
|-----------|-------------|
| `backend/` | Spring Boot 3.2, Java 17, MyBatis |
| `frontend/` | Vue 3 + Vite + Vue Router + Axios |
| `SPEC.md` | Full project specification |

## Key Commands

| Task | Command |
|------|---------|
| Build frontend | `npm run build` |
| Run backend | `mvn spring-boot:run` |
| Check DB connectivity | `mysql -h localhost -P 3306 -u root -php197027 -e "SELECT 1;"` |
| Kill port 9090 | `netstat -ano \| findstr "9090"` then `taskkill /F /PID <id>` |

## Known Issues

- **Lombok errors in IDE**: Compile works fine, ignore IDE warnings
- **403 Forbidden**: Check CORS config in SecurityConfig
- **Refresh page loses content**: Vue state initialization issue

## Database

- MySQL `social_platform` on localhost:3306
- Credentials in `backend/src/main/resources/application.yml`
- Default local password: `hp197027`
- Run `database.sql` to initialize tables
