# Sistema V com Docker Compose

## Configuração

Crie o arquivo local de variáveis a partir do exemplo:

```powershell
Copy-Item .env.example .env
```

Edite o `.env` e informe:

- a senha atual do PostgreSQL instalado no Windows em `POSTGRES_PASSWORD`;
- uma chave OpenAI nova em `JARVIT_OPENAI_API_KEY`;
- o modelo desejado em `JARVIT_OPENAI_MODEL`.

O arquivo `.env` é local e não deve ser enviado ao Git.

O mesmo `.env` também é carregado quando a aplicação é iniciada diretamente
com `mvn spring-boot:run`.

O Compose inicia somente a aplicação. Ela se conecta ao PostgreSQL instalado no
Windows usando `host.docker.internal`, na porta definida por `POSTGRES_PORT`.
O serviço do PostgreSQL do Windows precisa estar iniciado e aceitar conexões
vindas do Docker.

## Iniciar

```powershell
docker compose up --build -d
```

Abra `http://localhost:8383`.

## Acompanhar

```powershell
docker compose ps
docker compose logs -f aplicacao
```

## Parar

```powershell
docker compose down
```

O comando acima preserva banco, uploads e logs. Para remover também os volumes,
use `docker compose down -v` somente quando quiser apagar os uploads e logs do
container. Esse comando não remove o banco de dados instalado no Windows.
