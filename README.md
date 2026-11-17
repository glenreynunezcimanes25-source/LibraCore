# Libracore

A Java Swing desktop library management system.

## Features
- Book management
- Member management
- Borrow/return flow
- Overdue fine calculation
- Report generation
- Serialization persistence
- Built-in icon set in `resources/icons/`

Icon files are loaded from the project resources folder at runtime. Keep that folder alongside `src`, `bin`, and the project README.

## Compile (Windows PowerShell)
```powershell
javac -d bin (Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

## Run
```bash
java -cp bin gui.Main
```

Run both commands from the project directory. Libracore starts with an empty
library when no saved records exist and stores records in the `data` directory.
The application does not create sample books, members, or loans.
