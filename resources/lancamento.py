import matplotlib.pyplot as plt
import pandas as pd
import os

# Arquivos CSV
csv_file = "resources/lancamento.csv"
csv_file_rede = "resources/lancamentoRede.csv"

# Verifica se os arquivos existem
if not os.path.exists(csv_file):
    print(f"Erro: Arquivo '{csv_file}' não encontrado.")
    exit(1)

if not os.path.exists(csv_file_rede):
    print(f"Erro: Arquivo '{csv_file_rede}' não encontrado.")
    exit(1)

# Lê os metadados do primeiro arquivo
try:
    with open(csv_file, 'r') as file:
        for line in file:
            if line.startswith("#altura:"):
                altura = float(line.split(":")[1].strip())
            elif line.startswith("#distanciaRede:"):
                distancia_rede = float(line.split(":")[1].strip())
            elif line.startswith("#alturaRede:"):
                altura_rede = float(line.split(":")[1].strip())
            elif line.startswith("#planeta:"):
                planeta = line.split(":")[1].strip()
            elif line.startswith("#g:"):
                g = float(line.split(":")[1].strip())
except FileNotFoundError:
    print(f"Erro: '{csv_file}' não encontrado.")
    exit(1)

# Lê os dados do primeiro arquivo CSV
try:
    pontos = pd.read_csv(csv_file, comment='#')
except FileNotFoundError:
    print(f"Erro ao carregar '{csv_file}'.")
    exit(1)

# Remove NaN's ou infinitos
pontos = pontos.dropna()
pontos = pontos[~pontos.isin([float('inf'), float('-inf')]).any(axis=1)]

# Verificar se sobram dados
if pontos.empty:
    print("Todos os dados do primeiro arquivo são inválidos.")
    exit(1)

# Lê os dados do segundo arquivo CSV
try:
    pontos_rede = pd.read_csv(csv_file_rede, comment='#')
except FileNotFoundError:
    print(f"Erro ao carregar '{csv_file_rede}'.")
    exit(1)

# Remove NaN's ou infinitos do segundo arquivo
pontos_rede = pontos_rede.dropna()
pontos_rede = pontos_rede[~pontos_rede.isin([float('inf'), float('-inf')]).any(axis=1)]

# Verificar se sobram dados no segundo arquivo
if pontos_rede.empty:
    print("Todos os dados do segundo arquivo são inválidos.")
    exit(1)

# Título do gráfico
titulo = (
    f"Planeta: {planeta.capitalize()} ({g} m/s²)\n"
    f"Altura Inicial: {altura} m\n"
    f"Distância da Rede: {distancia_rede} m\n"
    f"Altura da Rede: {altura_rede} m"
)

# Configurações do gráfico
plt.style.use('dark_background')
fig, ax = plt.subplots(figsize=(12, 7))



# Plotar os dados
ax.plot(pontos['x'], pontos['y'], color='cyan', linewidth=2, label="Trajetória")
ax.plot(pontos_rede['x'], pontos_rede['y'], color='red', linewidth=2, label="Rede")

# Definições dos eixos
ax.axhline(y=0, color='gray', linestyle='--', linewidth=0.8)
ax.set_title(titulo, fontsize=16, fontweight='bold', loc='left', color='white', pad=20)
ax.set_xlabel("Distância (m)", fontsize=14, color='lightgray', labelpad=10)
ax.set_ylabel("Altura (m)", fontsize=14, color='lightgray', labelpad=10)

# Cores
ax.spines['top'].set_visible(False)
ax.spines['right'].set_visible(False)
ax.spines['left'].set_color('lightgray')
ax.spines['bottom'].set_color('lightgray')
ax.tick_params(colors='lightgray', which='both', labelsize=12)


# Adiciona o grid e legenda
ax.grid(color='gray', linestyle='--', linewidth=0.5, alpha=0.5)
ax.legend(facecolor='#2e2e2e', edgecolor='white', fontsize=12)

# Mostra o gráfico
plt.tight_layout()
plt.subplots_adjust(top=0.8)
plt.show()
