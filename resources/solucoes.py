import matplotlib.pyplot as plt
import pandas as pd
import sys

#Path dos ficheiros
vel_maxima = "resources/dadosSuperior.csv"
vel_minima = "resources/dadosInferior.csv"

#Le os dados de lançamento num dos ficheiros
try:
    with open(vel_maxima, 'r') as file:
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
    print(f"Erro: '{vel_maxima}' não encontrado.")
    sys.exit(1)

#Carrega os dados
try:
    pontos_maxima = pd.read_csv(vel_maxima, comment='#')
    pontos_minima = pd.read_csv(vel_minima, comment='#')
except FileNotFoundError:
    print("Falta algum dos ficheiros.")
    sys.exit(1)

#Remove NaN's ou infinitos
pontos_maxima = pontos_maxima.dropna()
pontos_minima = pontos_minima.dropna()
pontos_maxima = pontos_maxima[~pontos_maxima.isin([float('inf'), float('-inf')]).any(axis=1)]
pontos_minima = pontos_minima[~pontos_minima.isin([float('inf'), float('-inf')]).any(axis=1)]

#Verificar se sobraram dados depois de remover os invalidos
if pontos_maxima.empty or pontos_minima.empty:
    print("Todos os dados sao invalidos")
    sys.exit(1)

#Titulo do grafico
titulo = (
    f"Planeta: {planeta.capitalize()} ({g}m/s²)\n"
    f"Altura Inicial: {altura}m\n"
    f"Distância da Rede: {distancia_rede}m\n"
    f"Altura da Rede: {altura_rede}m"
)

#Janela do grafico
plt.style.use('dark_background')
fig, ax = plt.subplots(figsize=(12, 7))

#Ordenar os valores por x (desnecessário porque já vem ordenados do kotlin mas just in case)
pontos_maxima.sort_values(by='x', inplace=True)
pontos_minima.sort_values(by='x', inplace=True)

#Para combinar os valores
x_combined = sorted(set(pontos_maxima['x']).union(set(pontos_minima['x'])))
y_max = max(pontos_minima['y'])

#Para combinar os dominios
graf_vel_max = pontos_maxima.set_index('x').reindex(x_combined).interpolate().reset_index()
graf_vel_min = pontos_minima.set_index('x').reindex(x_combined).interpolate().reset_index()

#Onde o grafico velMax nao tem soluçao finita
mask_superior_nan = graf_vel_max['y'].isna()

#Preenche o espaço entre os dois graficos
ax.fill_between(
    graf_vel_max['x'],
    graf_vel_max['y'],
    graf_vel_min['y'],
    where=~mask_superior_nan,
    color='cyan',
    alpha = 0.3
)

#Preenche o espaço acimo do grafico velMin se velMax for infinito (nao existir nesses pontos)
ax.fill_between(
    graf_vel_min['x'][mask_superior_nan],
    graf_vel_min['y'][mask_superior_nan],
    y_max,
    color='cyan',
    alpha = 0.3
)

#Plot dos gráficos velMin e velMax
ax.plot(graf_vel_max['x'], graf_vel_max['y'], color='cyan', linewidth=2, label="Soluções")
ax.plot(graf_vel_min['x'], graf_vel_min['y'], color='cyan', linewidth=2)

#Definiçoes dos eixos
ax.axhline(y=0, color='gray', linestyle='--', linewidth=0.8)
ax.set_title(titulo, fontsize=16, fontweight='bold', loc='left', color='white', pad=20)
ax.set_xlabel("Ângulo (º)", fontsize=14, color='lightgray', labelpad=10)
ax.set_ylabel("Velocidade inicial (m/s)", fontsize=14, color='lightgray', labelpad=10)

#Cores
ax.spines['top'].set_visible(False)
ax.spines['right'].set_visible(False)
ax.spines['left'].set_color('lightgray')
ax.spines['bottom'].set_color('lightgray')
ax.tick_params(colors='lightgray', which='both', labelsize=12)

#limites do eixo y
ax.set_ylim(bottom=0, top=y_max + 5)
ax.xaxis.set_major_locator(plt.MultipleLocator(5))

#Adiciona o grid e legenda
ax.grid(color='gray', linestyle='--', linewidth=0.5, alpha=0.5)
ax.legend(facecolor='#2e2e2e', edgecolor='white', fontsize=12)

#Mostra o grafico
plt.tight_layout()
plt.subplots_adjust(top=0.8)
plt.show()