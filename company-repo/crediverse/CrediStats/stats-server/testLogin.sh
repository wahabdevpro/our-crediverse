echo '{"username": "stats_user", "password": "LmRB@hyE"}' | curl -XPOST -v -H "Content-Type: application/json" --data @/dev/stdin 'http://127.0.0.1:8801/credistats/login'
