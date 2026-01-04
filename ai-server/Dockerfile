FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# 8000번 포트에서 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]