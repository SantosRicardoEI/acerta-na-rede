import kotlin.math.*
import java.io.File
import javax.sound.sampled.AudioSystem

// readln com som "pop"
fun readlnS(): String {
    val input = readlnOrNull() ?: ""
    reproduzirSom("resources/sounds/pop.wav")
    return input
}

fun reproduzirSom(filePath: String) {
    Thread {
        try {
            val file = File(filePath)
            val audioInputStream = AudioSystem.getAudioInputStream(file)

            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)

            clip.start()

            clip.addLineListener { event ->
                if (event.type == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close()
                }
            }

            while (clip.isRunning) {
                Thread.sleep(10)
            }
        } catch (e: Exception) {
            println("Erro ao reproduzir o arquivo: ${e.message}")
        }
    }.start() //Inicia a execução noutra thread
}

fun contagemDecrescente(tempo: Int) {
    for (i in tempo downTo 1) {
        separarMenus()
        println("Lançamento em... $i")
        Thread.sleep(1000)
    }
}


//Limpa terminal
fun limparEcra() {
    print("\u001b[H\u001b[2J") //Código ANSI para mover o cursor para o topo e "apagar" o que está para trás
    System.out.flush()
}


//AutoExplicativo
fun enterParaContinuar() {
    println("Pressiona ENTER para continuar")
    readlnS()
}

//Imprime o grafico do lançamento (grafico: chart)
fun imprimeGraficoLancamento(
    grafico: Chart,
    acertouNaRede: Boolean,
    altura: Double,
    distanciaRede: Double,
    alturaRede: Double,
    alcance: Double,
    v0x: Double,
    v0y: Double,
    g: Double,
    anguloRad: Double,
    tentativas: Int,
    planeta: String,
    v0: Double,
    angulo: Double,
    tempoVoo: Double,
    tempoSubida: Double,
    alturaMaxima: Double,
    alturaNaRede: Double
) {

    val x = 0.0
    //Desenha a rede no gráfico
    desenharRedeNoGrafico(grafico, distanciaRede, alturaRede, altura)

    //Desenha a trajetória
    if (acertouNaRede) {
        desenharTrajetoriaAteRede(grafico, altura, v0y, v0x, g, x, distanciaRede)
    } else {
        desenharTrajetoriaAteFinal(
            grafico,
            altura,
            v0y,
            v0x,
            g,
            x,
            distanciaRede,
            alcance,
            alturaRede,
            anguloRad,
            alturaNaRede
        )
    }

    //Imprime o gráfico
    reproduzirSom("resources/sounds/drumRoll.wav")
    contagemDecrescente(3)
    separarMenus()
    println("\nAltura em função da posição:")
    grafico.draw()

    //Gera dados do lançamento
    gerarDadosLancamento(
        acertouNaRede, anguloRad, tentativas, distanciaRede, alturaRede, planeta,
        g, altura, v0, angulo, v0y, v0x, tempoVoo, tempoSubida, alcance, alturaMaxima, alturaNaRede
    )
}

//Cria os pontos da rede no grafico
fun desenharRedeNoGrafico(grafico: Chart, distanciaRede: Double, alturaRede: Double, altura: Double) {
    grafico.ponto(0.0, altura)
    var count = alturaRede
    while (count >= 0) {
        grafico.ponto(distanciaRede, count)
        count -= alturaRede / 15
    }
}

//Cria pontos grafico até a rede (usar quando acerta na rede)
fun desenharTrajetoriaAteRede(
    grafico: Chart, altura: Double, v0y: Double, v0x: Double, g: Double, x: Double, distanciaRede: Double
) {
    var posX = x
    do {
        val yEmX = altura + v0y * (posX / v0x) - (g / 2) * (posX / v0x).pow(2)
        grafico.ponto(posX, yEmX)
        posX += distanciaRede / 50
    } while (posX < distanciaRede)
}

//Cria pontos no grafico ate 2x a distancia da rede 
fun desenharTrajetoriaAteFinal(
    grafico: Chart,
    altura: Double,
    v0y: Double,
    v0x: Double,
    g: Double,
    x: Double,
    distanciaRede: Double,
    alcance: Double,
    alturaRede: Double,
    anguloRad: Double,
    alturaNaRede: Double
) {
    var posX = x
    while (posX < distanciaRede * 2) {
        val yEmX = altura + v0y * (posX / v0x) - (g / 2) * (posX / v0x).pow(2)
        if (yEmX > 0) {
            grafico.ponto(posX, yEmX)
        }
        posX += if (alturaNaRede < alturaRede) {
            alcance / 50
        } else {
            distanciaRede / 50
        }
    }
}

//Chama a funçao imprimeDadosLancamento com a descriçao do movimento
fun gerarDadosLancamento(
    acertouNaRede: Boolean,
    anguloRad: Double,
    tentativas: Int,
    distanciaRede: Double,
    alturaRede: Double,
    planeta: String,
    g: Double,
    altura: Double,
    v0: Double,
    angulo: Double,
    v0y: Double,
    v0x: Double,
    tempoVoo: Double,
    tempoSubida: Double,
    alcance: Double,
    alturaMaxima: Double,
    alturaNaRede: Double
) {
    val descricaoDoMovimento = when {
        alcance > distanciaRede -> "         O jogador passou por cima!"
        alcance < distanciaRede -> "           O jogador caiu antes!"
        else -> ""
    }

    imprimeDadosLancamento(
        alturaNaRede,
        anguloRad,
        tentativas,
        descricaoDoMovimento,
        distanciaRede,
        alturaRede,
        acertouNaRede,
        planeta,
        g,
        altura,
        v0,
        angulo,
        v0y,
        v0x,
        tempoVoo,
        tempoSubida,
        alcance,
        alturaMaxima
    )
}

//Se nao houver movimento no eixo X, jogador nao saiu do sitio -> descreve o que aconteceu sem mostrar grafico
fun descreveMovimento(
    v0: Double,
    altura: Double,
    angulo: Double,
    tempoVoo: Double,
    alturaMaxima: Double,
    tentativas: Int
) {
    if ((v0 == 0.0 && altura == 0.0) || (altura == 0.0 && angulo == 0.0)) {
        reproduzirSom("resources/sounds/drumRoll.wav")
        contagemDecrescente(3)
        reproduzirSom("resources/sounds/falhou.wav")
        imprimirComoEscrita("\nO jogador não se moveu! Não acertaste na rede :(", 15, true)
        println("Tentativas: $tentativas\n")
    }
    // 1 -Se v0 = 0 -> O projetil foi largado a Xm de altura.
    else if (v0 == 0.0 && altura != 0.0) {
        reproduzirSom("resources/sounds/drumRoll.wav")
        contagemDecrescente(3)
        reproduzirSom("resources/sounds/falhou.wav")
        imprimirComoEscrita(
            "\nO jogador foi largado a ${arredondar(altura)}m de altura! Caiu no chão em ${
                arredondar(
                    tempoVoo
                )
            }s e não acertou na rede :(", 15, true
        )
        println("Tentativas: $tentativas\n")
    }

    // 2 - Se angulo = 0 -> O projetil foi lançado para cima.
    else if (angulo == 90.0 && v0 != 0.0) {
        reproduzirSom("resources/sounds/drumRoll.wav")
        contagemDecrescente(3)
        reproduzirSom("resources/sounds/falhou.wav")
        imprimirComoEscrita(
            "\nO jogador foi lançado para cima. De ${arredondar(altura)}m para ${arredondar(alturaMaxima)}m de altura, caiu no chão e não acertou na rede :(",
            15,
            true
        )
        println("Tentativas: $tentativas\n")
    }
}

//Pede valores de lançamento (angulo e velocidade)
fun pedeAnguloEVelocidade(altura: Double): Pair<Double, Double> {
    val angulo = pedeAngulo()
    val v0 = if (altura != 0.0 || angulo != 0.0) {
        pedeVelocidade()
    } else {
        0.0
    }
    return Pair(angulo, v0)
}

//Calcula os dados do lançamento
fun calcularDadosLancamento(
    angulo: Double,
    v0: Double,
    altura: Double,
    g: Double,
    distanciaRede: Double,
    alturaRede: Double,
): Map<String, Any> {
    val anguloRad = pRadianos(angulo)
    val v0y = v0 * sin(anguloRad)
    val v0x = v0 * cos(anguloRad)

    val tempoSubida = v0y / g
    val alturaMaxima = altura + v0y * tempoSubida - (g / 2) * tempoSubida.pow(2)
    val tempoVoo = fResolvente(-(g / 2), v0y, altura)
    val alcance = v0 * cos(anguloRad) * tempoVoo
    altura + v0 * sin(anguloRad) * (distanciaRede / (v0 * cos(anguloRad))) - (g / 2) * (distanciaRede / (v0 * cos(
        anguloRad
    ))).pow(2)
    val alturaNaRede =
        altura + v0 * sin(anguloRad) * (distanciaRede / (v0 * cos(anguloRad))) - (g / 2) * (distanciaRede / (v0 * cos(
            anguloRad
        ))).pow(2)
    val acertouNaRede = (alturaNaRede in 0.0..alturaRede)

    return mapOf(
        "anguloRad" to anguloRad,
        "v0y" to v0y,
        "v0x" to v0x,
        "tempoVoo" to tempoVoo,
        "alcance" to alcance,
        "tempoSubida" to tempoSubida,
        "alturaMaxima" to alturaMaxima,
        "acertouNaRede" to acertouNaRede,
        "alturaNaRede" to alturaNaRede,

        )
}

//Calcula coordenadas do lancamento para usar em grafico no python, exporta os dados para .csv, e gera o gráfico
fun lancamentoPython(
    planeta: String,
    g: Double,
    altura: Double,
    v0y: Double,
    v0x: Double,
    distanciaRede: Double,
    alturaRede: Double,
    alturaNaRede: Double,
    alcance: Double
) {
    val graficoLancamento = mutableListOf<Pair<Double, Double>>() // Grafico de lançamento
    val graficoRede = mutableListOf<Pair<Double, Double>>() // Grafico da rede

    graficoLancamento.add(Pair(0.0, altura))
    graficoRede.add(Pair(distanciaRede, 0.0))
    graficoRede.add(Pair(distanciaRede, alturaRede))

    var x = 0.0
    while (x < distanciaRede * 2) {
        val y = altura + v0y * (x / v0x) - (g / 2) * (x / v0x).pow(2)
        if (y > 0) {
            graficoLancamento.add(Pair(x, y))
        }
        x += alcance / 100
    }

    exportarDadosGrafico(graficoLancamento, "resources/lancamento.csv", altura, distanciaRede, alturaRede, planeta, g)
    exportarDadosGrafico(graficoRede, "resources/lancamentoRede.csv", altura, distanciaRede, alturaRede, planeta, g)
    executarScriptPython("resources/lancamento.py")
    //executarPython("resources/lancamento")
    separarMenus()

}

//Cria coordenadas soluçao para grafico, exporta os dados para .csv, e executa grafico em python
fun solucoesGrafico(
    g: Double,
    distanciaRede: Double,
    altura: Double,
    alturaRede: Double,
    planeta: String
) {
    //Path dos .csv
    val graficoSuperior = "resources/dadosSuperior.csv"
    val graficoInferior = "resources/dadosInferior.csv"
    val scriptPython = "resources/solucoessolucoes.py"

    val coordenadasVelMax = mutableListOf<Pair<Double, Double>>() // Gráfico de Velocidade Máxima
    val coordenadasVelMin = mutableListOf<Pair<Double, Double>>() // Gráfico de Velocidade Mínima

    var anguloGraus = 0.1
    while (anguloGraus in 0.1..89.9) {
        val anguloRads = Math.toRadians(anguloGraus) // Converte graus para radianos
        val velMin = sqrt(
            (g * distanciaRede.pow(2)) /
                    (2 * cos(anguloRads).pow(2) * (distanciaRede * tan(anguloRads) - 0 + altura))
        )
        val velMax = sqrt(
            (g * distanciaRede.pow(2)) /
                    (2 * cos(anguloRads).pow(2) * (distanciaRede * tan(anguloRads) - alturaRede + altura))
        )

        coordenadasVelMin.add(Pair(anguloGraus, velMin))
        coordenadasVelMax.add(Pair(anguloGraus, velMax))

        anguloGraus += 0.1
    }

    // Exportar dados para CSV
    exportarDadosGrafico(coordenadasVelMax, graficoSuperior, altura, distanciaRede, alturaRede, planeta, g)
    exportarDadosGrafico(coordenadasVelMin, graficoInferior, altura, distanciaRede, alturaRede, planeta, g)

    // Executar script Python
    executarScriptPython("resources/solucoes.py")
    //executarPython("resources/Main")
}

//Calcula as coordenadas que são solução (velMax e velMin para acertar em cada angulo)
fun solucoesTabela(planeta: String, g: Double, altura: Double, distanciaRede: Double, alturaRede: Double) {
    val coordenadasVelMax = mutableListOf<Pair<Double, Double>>() // Gráfico de Velocidade Máxima
    val coordenadasVelMin = mutableListOf<Pair<Double, Double>>() // Gráfico de Velocidade Mínima

    for (anguloGraus in 0..89) {
        val anguloRads = Math.toRadians(anguloGraus.toDouble()) // Converte para radianos
        var velMax = sqrt(
            (g * distanciaRede.pow(2)) /
                    (2 * cos(anguloRads).pow(2) * (distanciaRede * tan(anguloRads) - alturaRede + altura))
        )
        if (velMax.isNaN()) velMax = -1.0

        var velMin = sqrt(
            (g * distanciaRede.pow(2)) /
                    (2 * cos(anguloRads).pow(2) * (distanciaRede * tan(anguloRads) - 0 + altura))
        )
        if (velMin.isInfinite()) velMin = -1.0

        coordenadasVelMax.add(Pair(anguloGraus.toDouble(), velMax))
        coordenadasVelMin.add(Pair(anguloGraus.toDouble(), velMin))
    }

    imprimirTabela(planeta, g, altura, distanciaRede, alturaRede, coordenadasVelMax, coordenadasVelMin)
}

//Imprime a tabela das soluçoes (e formata os valores)
fun imprimirTabela(
    planeta: String,
    g: Double,
    altura: Double,
    distanciaRede: Double,
    alturaRede: Double,
    coordenadasVelMax: List<Pair<Double, Double>>,
    coordenadasVelMin: List<Pair<Double, Double>>
) {
    println(
        """
    |==========================================
    |            SOLUÇÕES
    |==========================================
    |🌍 Planeta:           ${planeta.replaceFirstChar { it.uppercase() }} (${g} m/s²)
    |🧨 Altura do Canhão:  $altura m
    |📏 Distância da Rede: ${arredondar(distanciaRede)} m
    |📐 Altura da Rede:    ${arredondar(alturaRede)} m
    |==========================================
    |Ângulo (º)       Velocidade (m/s)
    |==========================================
""".trimMargin()
    )

    for (i in coordenadasVelMax.indices) {
        val anguloGraus = coordenadasVelMax[i].first.toInt()
        val velocidadeMinima = arredondar(coordenadasVelMin[i].second)
        val velocidadeMaxima = arredondar(coordenadasVelMax[i].second)

        val velocidadeMinimaFormatada = if (velocidadeMinima == -1.0) "∞" else String.format("%.2f", velocidadeMinima)
        val velocidadeMaximaFormatada = if (velocidadeMaxima == -1.0) "∞" else String.format("%.2f", velocidadeMaxima)

        println(String.format("%-15d %s - %s", anguloGraus, velocidadeMinimaFormatada, velocidadeMaximaFormatada))
    }
    println("==========================================")
}

//Exporta pontos do grafico para um .csv
fun exportarDadosGrafico(
    pontos: MutableList<Pair<Double, Double>>,
    nomeArquivo: String,
    altura: Double,
    distanciaRede: Double,
    alturaRede: Double,
    planeta: String,
    g: Double
) {
    val file = File(nomeArquivo)
    file.printWriter().use { out ->
        //Adiciona as condiçoes de lançamento (em comentario (#))
        out.println("#planeta: $planeta")
        out.println("#g: $g")
        out.println("#altura: $altura")
        out.println("#distanciaRede: $distanciaRede")
        out.println("#alturaRede: $alturaRede")
        //Adiciona coordenadas
        out.println("x,y") //Nome das colunas
        pontos.forEach { (x, y) ->
            out.println("$x,$y")
        }
    }
}

//Para executar py.exe
fun executarPython(executablePath: String, args: List<String> = emptyList()) {
    val processBuilder = ProcessBuilder(executablePath, *args.toTypedArray())
    val process = processBuilder.start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        println("Erro ao executar $executablePath: $exitCode")
    }
}

//Para executar .py
fun executarScriptPython(scriptPath: String, args: List<String> = emptyList()) {
    val pythonExecutable = "python/bin/python3.13" // Ajuste para um caminho absoluto, se necessário
    val command = listOf(pythonExecutable, scriptPath) + args

    val processBuilder = ProcessBuilder(command)
    processBuilder.redirectErrorStream(true) //Redireciona erros para stdout
    try {
        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().readText()
        val errorOutput = process.errorStream.bufferedReader().readText() //Captura os erros
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            println("Erro ao iniciar o script Python:  $exitCode")
        } else {
            //println("Script Python executado com sucesso.")
        }
    } catch (e: Exception) {
        println("Erro ao iniciar o script Python: ${e.message}")
    }
}

//Imprime o Menu Principal com as condicoes de lançamento atuais
fun imprimeTitulo(g: Double, altura: Double, planeta: String, distanciaRede: Double, alturaRede: Double) {
    println()
    println(
        """
        ==========================================
                 🚀 PROJETO DE FÍSICA 🚀
                    🌟 Acerta na Rede! 🌟
        ==========================================
        🌍 Planeta atual:     ${planeta.replaceFirstChar { it.uppercase() }} (${g} m/s²)
        🧨 Altura do Canhão:  ${altura}m
        📏 Distancia da Rede: ${arredondar(distanciaRede)}m
        📐 Altura da Rede:    ${arredondar(alturaRede)}m
        ==========================================
                🔽 MENU PRINCIPAL 🔽
        ==========================================
         1 - Lançar Jogador
         2 - Escolher Planeta
         3 - Escolher Rede
         4 - Escolher Altura do Canhão
         5 - Instruções
         6 - Soluções
         7 - Informações Sobre o Projeto
         0 - Sair
        ==========================================
    """.trimIndent()
    )
}

//Arredonda Doubles para duas casas decimais (Precisao de dados 0.01)
fun arredondar(num: Double): Double {
    var numero = (round(num * 100)) / 100
    if (numero == -0.0) {
        numero = 0.0
    }
    return numero
}

//Pede ao utilizar, verifica, e retorna uma opcao valida
fun validaOpcao(): Int {
    do {
        val opcao = readlnS().toIntOrNull()
        if (opcao != null && opcao in 0..7) {
            return opcao
        } else {
            reproduzirSom("resources/sounds/erro.wav")
            println("Seleciona uma opção disponível")
        }
    } while (true)
}

//Converte graus para radianos
fun pRadianos(a: Double) = (a * PI / 180)

//Retorna a gravidade do planeta escolhido
fun gravidade(planeta: String): Double {
    when (planeta) {
        "mercurio", "mercúrio" -> return 3.7
        "venus", "vénus" -> return 8.7
        "terra" -> return 9.8
        "marte" -> return 3.7
        "jupiter", "júpiter" -> return 24.9
        "saturno" -> return 10.5
        "urano" -> return 8.6
        "neptuno" -> return 11.8
        "plutao", "plutão" -> return 0.6
        "lua" -> return 1.6
    }
    return -1.0
}

//Pede ao utilizador a distancia e altura da rede. Retorna valores validados
fun pedeRede(): Pair<Double, Double> {
    var distanciaRede = 0.0
    var alturaRede = 0.0
    do {
        imprimirComoEscrita("Introduz a distancia da rede:", 15, true)
        distanciaRede = readlnS().toDoubleOrNull() ?: -1.0
        if (distanciaRede <= 0) {
            reproduzirSom("resources/sounds/erro.wav")
            println("Distancia invalida, o valor deve ser maior que 0.")
        }
    } while (distanciaRede <= 0)

    do {
        imprimirComoEscrita("Introduz a altura da rede:", 15, true)
        alturaRede = readlnS().toDoubleOrNull() ?: -1.0
        if (alturaRede <= 0) {
            reproduzirSom("resources/sounds/erro.wav")
            println("Altura invalida, o valor deve ser maior que 0.")
        }
    } while (alturaRede <= 0)
    return Pair(distanciaRede, alturaRede)
}

//Pede ao utilizador um planeta do sistema solar e valida
fun pedePlaneta(): Pair<String, Double> {
    imprimirComoEscrita("Introduz o nome de um planeta do sistema solar ou Enter para escolher a Terra", 10, true)
    println("(Escreve \"outro\" para definir um planeta personalizado)")
    do {
        val entrada = readlnS().lowercase()

        when {
            entrada == "" -> return Pair("terra", 9.8) // Padrão: Terra
            entrada == "outro" -> {
                val (nomePersonalizado, gravidadePersonalizada) = pedePlanetaPersonalizado()
                if (gravidadePersonalizada > 0) {
                    return Pair(nomePersonalizado, gravidadePersonalizada)
                }
            }

            gravidade(entrada) != -1.0 -> return Pair(entrada, gravidade(entrada)) // Planeta predefinido
            else -> {
                reproduzirSom("resources/sounds/erro.wav")
                println("Planeta inválido. Tenta novamente.")
            }
        }
    } while (true)
}

//Pede ao utilizador para criar um planeta (nome e gravidade) e retorna o par (planeta, gravidade)
fun pedePlanetaPersonalizado(): Pair<String, Double> {
    imprimirComoEscrita("Introduz o nome do planeta:", 15, true)
    val nomePlaneta = readlnS().lowercase()

    var gravidadePlaneta: Double? = null
    imprimirComoEscrita("Introduz a gravidade do planeta (m/s²):", 15, true)
    while (gravidadePlaneta == null || gravidadePlaneta <= 0) {
        gravidadePlaneta = readlnS().toDoubleOrNull()

        if (gravidadePlaneta == null || gravidadePlaneta <= 0) {
            reproduzirSom("resources/sounds/erro.wav")
            println("Gravidade inválida. O valor deve ser um número positivo.")
        }
    }

    return Pair(nomePlaneta, gravidadePlaneta)
}

//Formula resolvente que retorna a maior solução
fun fResolvente(a: Double, b: Double, c: Double): Double {
    val d = b * b - 4 * a * c
    if (d < 0) {
        return -1.0
    }
    val sol1 = (-b + sqrt(d)) / (2 * a)
    val sol2 = (-b - sqrt(d)) / (2 * a)
    if (sol1 > sol2) {
        return sol1
    } else return sol2
}

//Pede ao utilizar, verifica, e retorna uma altura valida
fun pedeAltura(): Double {
    do {
        imprimirComoEscrita("Introduz a altura do canhão (ou Enter para altura 0.0m):", 15, true)
        val altura = readlnS()
        if (altura == "") {
            return 0.0
        }
        val alturaValidada = altura.toDoubleOrNull() ?: -1.0
        if (alturaValidada >= 0.0) {
            return alturaValidada
        } else {
            reproduzirSom("resources/sounds/erro.wav")
            println("Altura inválida. A altura deve ser um valor positivo")
        }
    } while (true)
}

//Pede ao utilizar, verifica, e retorna uma velocidade valida
fun pedeVelocidade(): Double {
    do {
        imprimirComoEscrita("Introduz a velocidade inicial", 15, true)
        val v0 = readlnS().toDoubleOrNull() ?: -1.0
        if (v0 >= 0) {
            return v0
        } else {
            reproduzirSom("resources/sounds/erro.wav")
            println("Velocidade inválida. A velocidade deve ser um valor positivo")
        }
    } while (true)
}

//Pede ao utilizar, verifica, e retorna um angulo em graus valido
fun pedeAngulo(): Double {
    do {
        imprimirComoEscrita("Introduz o ângulo (0º-90º)", 15, true)
        val angulo = readlnS().toDoubleOrNull() ?: -1.0
        if (angulo in (0.0..90.0)) {
            return angulo
        } else {
            reproduzirSom("resources/sounds/erro.wav")
            println("Ângulo inválido")
        }
    } while (true)
}


//Imprime os dados de lançamento
fun imprimeDadosLancamento(
    alturaNaRede: Double,
    anguloEmRad: Double,
    tentativas: Int,
    descricaoDoMovimento: String,
    distanciaRede: Double,
    alturaRede: Double,
    acertouNaRede: Boolean,
    planeta: String,
    g: Double,
    altura: Double,
    v0: Double,
    angulo: Double,
    v0y: Double,
    v0x: Double,
    tempoVoo: Double,
    tempoSubida: Double,
    alcance: Double,
    alturaMaxima: Double
) {
    if (!acertouNaRede) {
        println(
            """
        ==========================================
                ❌ NÃO ACERTASTE NA REDE ❌
        $descricaoDoMovimento
        ==========================================
                 📊 DADOS DO LANÇAMENTO 📊
        ==========================================
        🌍 Planeta:                  ${planeta.replaceFirstChar { it.uppercase() }}
        🌌 Aceleração Gravítica:     ${arredondar(g)}m/s²
        📏 Distancia da Rede:        ${arredondar(distanciaRede)}m
        📐 Altura da Rede:           ${arredondar(alturaRede)}m
        🏔️  Altura Inicial:           ${arredondar(altura)}m
        🚀 Velocidade Inicial:       ${arredondar(v0)}m/s
        🎯 Ângulo de Lançamento:     ${arredondar(angulo)}º
        ⬆️  Velocidade Vertical:      ${arredondar(v0y)}m/s
        ➡️  Velocidade Horizontal:    ${arredondar(v0x)}m/s
        ⏱️  Tempo de Voo:             ${arredondar(tempoVoo)}s
        ⬆️  Tempo de Subida:          ${arredondar(tempoSubida)}s
        📏 Alcance:                  ${arredondar(alcance)}m
        ⛰️  Altura Máxima:            ${arredondar(alturaMaxima)}m
        📐 Altura na posição Rede:   ${if (alturaNaRede > 0) "${(arredondar(alturaNaRede))}m" else "ND"}
        ==========================================
        Tentativas: $tentativas
        ==========================================
    """.trimIndent()
        )
        reproduzirSom("resources/sounds/falhou.wav")
    } else {
        println(
            """
        ==========================================
                       🎉 PARABÉNS! 🎉
                  🏆 Acertaste na Rede! 🏆
                       Tentativas : $tentativas
        ==========================================
                 📊 DADOS DO LANÇAMENTO 📊
        ==========================================
        🌍 Planeta:                  ${planeta.replaceFirstChar { it.uppercase() }}
        🌌 Aceleração Gravítica:     ${arredondar(g)} m/s²
        📏 Distancia da Rede:        ${arredondar(distanciaRede)}m
        📐 Altura da Rede:           ${arredondar(alturaRede)}m
        🏔️  Altura Inicial:           ${arredondar(altura)} m
        🚀 Velocidade Inicial:       ${arredondar(v0)} m/s
        🎯 Ângulo de Lançamento:     ${arredondar(angulo)}º
        ⬆️  Velocidade Vertical:      ${arredondar(v0y)} m/s
        ➡️  Velocidade Horizontal:    ${arredondar(v0x)} m/s
        ⏱️  Tempo de Voo:             ${arredondar(tempoVoo)} s
        ⬆️  Tempo de Subida:          ${arredondar(tempoSubida)} s
        📏 Alcance:                  ${arredondar(distanciaRede)} m
        ⛰️  Altura Máxima:            ${arredondar(alturaMaxima)} m
        📐 Altura na posição Rede:   ${arredondar(alturaNaRede)}m
        ==========================================

    """.trimIndent()
        )
        reproduzirSom("resources/sounds/acertou.wav")
    }
}

//Imprime as instruçoes (com os valores selecionados para a rede)
fun imprimeInstrucoes(distanciaRede: Double, alturaRede: Double) {
    reproduzirSom("resources/sounds/keyboard.wav")
    imprimirComoEscrita(
        """

        ==========================================
                 🏅 INSTRUÇÕES DO JOGO 🏅
        ==========================================

        🎢 Bem-vindo ao Recinto de Jogos Radicais! 🎢

        🌟 Desafio: Lança um jogador com o canhão e
           tenta acertar na rede! 🌟

        🛡️ Para garantir a segurança do jogador,
           uma rede foi posicionada:
        - Distancia: $distanciaRede metros
        - Altura: $alturaRede metros

        🌟 Para jogar, introduz os seguintes dados:
        - Velocidade inicial.
        - Ângulo de lançamento.

        🚀 O jogador está a contar contigo!
            Não podes falhar! 🚀

        ==========================================
             🔍 INFORMAÇÕES ADICIONAIS 🔍
        ==========================================
        🌍 O jogo inicia-se no planeta Terra.
        🔄 Podes alterar ou adicionar o teu próprio
           planeta na opção 2 do Menu Principal.
        🎯 Também no Menu Principal podes:
         - Escolher a altura e distancia da rede.
         - Escolher a altura do canhão.
         - Visualizar o gráfico das soluções para 
         as condições de lançamento escolhidas.

        Boa sorte!!! 🎉
        ==========================================
        Pressiona Enter para continuar
    """.trimIndent(), 5, true
    )
    readlnS()
}

//Imprime informaçoes sobre o projeto
fun imprimeInfos() {
    reproduzirSom("resources/sounds/fanfare.wav")
    imprimirComoEscrita(
        """

        ==========================================
         📚 PROJETO DE FUNDAMENTOS DE FÍSICA 📚
        ==========================================
        Este projeto foi realizado no âmbito da
        disciplina de Fundamentos de Física pelos:

        👨‍🎓 Alunos:
        ------------------------------------------
        🔹 Ricardo Santos
        🔹 Nuno Tainha
        🔹 Duarte Martins
        🔹 Duarte Veríssimo
        ------------------------------------------
        🏫 Universidade Lusófona - DEISI
        💻 Engenharia Informártica

        🗓️ Ano Letivo: 2024/2025
        📆 (1º Semestre)
        ==========================================
        Pressiona Enter para continuar
    """.trimIndent(), 10, true
    )
}

//Separa os menus aplicando multiplas quebras de linhas e limpa terminal (se nao receber true)
fun separarMenus(soAfastar: Boolean = false) {
    println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
    if (!soAfastar) {
        limparEcra()
    }
}

//Imprime fim do programa
fun imprimeFim() {
    reproduzirSom("resources/sounds/end.wav")
    imprimirComoEscrita(
        """
                                         _.oo.
                 _.u[[/;:,.         .odMMMMMM'
              .o888UU[[[/;:-.  .o@P^    MMM^
             oN88888UU[[[/;::-.        dP^
            dNMMNN888UU[[[/;:--.   .o@P^
           ,MMMMMMN888UU[[/;::-. o@^
           NNMMMNN888UU[[[/~.o@P^
           888888888UU[[[/o@^-..
          oI8888UU[[[/o@P^:--..
       .@^  YUU[[[/o@^;::---..
     oMP     ^/o@P^;:::---..
  .dMMM    .o@^ ^;::---...
 dMMMMMMM@^`       `^^^^
YMMMUP^
 ^^
                                                 
""", 7, true
    )
    imprimirComoEscrita("Até à próxima!", 20, true)
    Thread.sleep(2000)
}

//Pergunta ao utilizador se quer tentar outra vez e retorna boolean
fun tentarOutraVez(): Int {
    do {
        println("Tentar outra vez? (ou ENTER para abrir gráfico Python)")
        val resposta = readlnS()
        when (resposta.lowercase()) {

            "" -> return 2
            "s", "sim" -> return 1
            "n", "nao", "não" -> return 0
            else -> {
                reproduzirSom("resources/sounds/erro.wav")
                println("Resposta inválida. Escolha \"sim\" \"nao\" ou ENTER.")
            }
        }
    } while (true)
}

//Imprimie caracter por caracter (delay personalizavel)
fun imprimirComoEscrita(texto: String, delay: Long, espera: Boolean = false) {
    val thread = Thread {
        for (char in texto) {
            print(char)
            Thread.sleep(delay)
        }
        println()
    }
    thread.start()
    if (espera) {
        thread.join()
    }
}


fun main() {

    separarMenus()
    readlnS()

    var planeta = "terra"
    var g = gravidade(planeta)
    var haResistencia = false
    var distanciaRede = 40.0
    var alturaRede = 5.0
    var altura = 0.0

    val larguraGrafico = 100
    val alturaGrafico = 20
    val grafico = Chart(larguraGrafico, alturaGrafico)

    separarMenus()
    imprimeInstrucoes(distanciaRede, alturaRede)
    separarMenus()
    do {
        separarMenus()
        println("Resistencia do ar: $haResistencia")
        reproduzirSom("resources/sounds/menu.wav")
        imprimeTitulo(g, altura, planeta, distanciaRede, alturaRede)
        val opcao = validaOpcao()
        when (opcao) {
            1 -> {
                var tentativas = 0
                do {
                    tentativas += 1
                    val (angulo, v0) = pedeAnguloEVelocidade(altura)
                    val dados = calcularDadosLancamento(angulo, v0, altura, g, distanciaRede, alturaRede)
                    val anguloRad = dados["anguloRad"] as Double
                    val v0y = dados["v0y"] as Double
                    val v0x = dados["v0x"] as Double
                    val tempoVoo = dados["tempoVoo"] as Double
                    val alcance = dados["alcance"] as Double
                    val tempoSubida = dados["tempoSubida"] as Double
                    val alturaMaxima = dados["alturaMaxima"] as Double
                    val acertouNaRede = dados["acertouNaRede"] as Boolean
                    val alturaNaRede = dados["alturaNaRede"] as Double

                    if (v0 == 0.0 && altura == 0.0 || altura == 0.0 && angulo == 0.0 || angulo == 90.0) {
                        separarMenus()
                        descreveMovimento(v0, altura, angulo, tempoVoo, alturaMaxima, tentativas)
                    } else {
                        separarMenus()
                        imprimeGraficoLancamento(
                            grafico,
                            acertouNaRede,
                            altura,
                            distanciaRede,
                            alturaRede,
                            alcance,
                            v0x,
                            v0y,
                            g,
                            anguloRad,
                            tentativas,
                            planeta,
                            v0,
                            angulo,
                            tempoVoo,
                            tempoSubida,
                            alturaMaxima,
                            alturaNaRede
                        )
                    }
                    var tentarOutraVez = 4
                    do {
                        tentarOutraVez = tentarOutraVez()
                        if (tentarOutraVez == 2 && (alcance != 0.0 || altura != alturaMaxima)) {
                            lancamentoPython(
                                planeta, g, altura, v0y, v0x, distanciaRede, alturaRede, alturaNaRede, alcance
                            )
                        } else if (tentarOutraVez == 2 && alcance == 0.0 && altura == alturaMaxima) {
                            reproduzirSom("resources/sounds/erro.wav")
                            println("O projetil não se moveu. Sem gráfico para mostrar")
                        }
                    } while (tentarOutraVez == 2)
                } while (tentarOutraVez == 1)
            }

            2 -> {
                separarMenus()
                val (planetaEscolhido, gravidadeEscolhida) = pedePlaneta()
                planeta = planetaEscolhido
                g = gravidadeEscolhida
                reproduzirSom("resources/sounds/planeta.wav")
                imprimirComoEscrita("Escolheste ${planeta.capitalize()} (${g}m/s²)!", 10, true)
                Thread.sleep(2000)
            }

            3 -> {
                separarMenus()
                val rede = pedeRede()
                distanciaRede = rede.first
                alturaRede = rede.second
                reproduzirSom("resources/sounds/rede.wav")
                imprimirComoEscrita("A rede foi colocada a ${distanciaRede}m e tem ${alturaRede}m de altura!", 10, true)
                Thread.sleep(2000)
            }

            4 -> {
                separarMenus()
                altura = pedeAltura()
                reproduzirSom("resources/sounds/canhao.wav")
                imprimirComoEscrita("O canhão foi colocado a ${altura}m de altura!", 10, true)
                Thread.sleep(2000)
            }

            5 -> {
                separarMenus()
                imprimeInstrucoes(distanciaRede, alturaRede)
            }

            6 -> {
                separarMenus(true)
                solucoesTabela(planeta, g, altura, distanciaRede, alturaRede)
                solucoesGrafico(g, distanciaRede, altura, alturaRede, planeta)
                Thread.sleep(1000)
            }

            7 -> {
                separarMenus()
                imprimeInfos()
                readlnS()
            }

            8 -> {
                haResistencia = !haResistencia
                limparEcra()
            }
        }
    } while (opcao != 0)
    separarMenus()
    imprimeFim()
}


