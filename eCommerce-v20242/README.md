# Projeto de Testes de Software - Terceira Unidade

Este projeto foi desenvolvido como parte das atividades de testes de software da terceira unidade. Ele inclui funcionalidades básicas de uma aplicação e testes que utilizam ferramentas para cobertura de código e análise de mutantes.

---

## Como Rodar o Projeto

1. Abra o projeto na sua IDE de preferência.
2. Localize a classe `CompraApplication.java`.
3. Execute o método `main` da classe para rodar o projeto.

---

##  Como Rodar os Testes e Gerar o `.jar`

É necessário ter o **Maven** instalado na sua máquina para rodar os testes e gerar o arquivo `.jar`. 

Execute o seguinte comando no terminal na raiz do projeto:

    mvn clean install

## Verificação de Cobertura de Testes

A cobertura de testes foi verificada utilizando a funcionalidade nativa da IDE IntelliJ IDEA. Caso deseje verificar:

Navegue até o pacote ou teste desejado.
Clique com o botão direito sobre o arquivo.
Escolha "More Run/Debug" > "Run <Nome_do_Teste> with Coverage".

Os resultados de cobertura serão exibidos diretamente na IDE.

Ou execute na raiz do projeto:
```
mvn test
```
## Cobertura de Mutantes com PIT

Para realizar a cobertura de mutantes, foi utilizada a ferramenta PIT (Pitest). Caso queira utilizá-la:

Certifique-se de que o Maven está instalado na sua máquina.
No terminal, na raiz do projeto, execute o comando:

    mvn test-compile org.pitest:pitest-maven:mutationCoverage

Os resultados serão gerados e podem ser analisados no relatório de cobertura de mutantes, localizado em target/pit-reports/index.html.
Basta clicar no index.html para ver o relatório
