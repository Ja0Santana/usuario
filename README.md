<div align="center">
  <h1>User API - Agendador de Tarefas</h1>
  <p><i>Microsserviço responsável pela gestão de usuários e autenticação do sistema.</i></p>
</div>

<hr>

<h2>📝 Descrição do Projeto</h2>
<p>Esta API gerencia todo o fluxo de usuários do ecossistema de agendamento de tarefas, incluindo cadastro, persistência de perfis e regras de negócio relacionadas à identidade dos utilizadores.</p>

<p><b>Este serviço é parte integrante de uma arquitetura de microsserviços.</b> Para visualizar e rodar o sistema completo, acesse o repositório principal:</p>
<p>🔗 <b>BFF Orquestrador:</b> <a href="https://github.com/Ja0Santana/BFF-Agendador">github.com/Ja0Santana/BFF-Agendador</a></p>

<hr>

<h2>🐋 Docker Hub - Imagem Oficial</h2>
<p>A imagem isolada deste serviço pode ser obtida via:</p>
<pre><code>docker pull joaopaul0/api-usuario:latest</code></pre>

<hr>

<h2>🛠️ Tecnologias e Ferramentas</h2>
<ul>
  <li><b>Java 17+ & Spring Boot 3</b></li>
  <li><b>PostgreSQL</b> (Persistência de dados relacionais)</li>
  <li><b>Docker</b> (Containerização)</li>
  <li><b>SonarQube</b> (Inspeção de segurança e qualidade)</li>
  <li><b>Swagger/OpenAPI</b> (Documentação dos endpoints de usuário)</li>
</ul>

<hr>

<h2>🛡️ Qualidade e Engenharia</h2>
<ul>
  <li><b>SOLID:</b> Implementação de código limpo e desacoplado.</li>
  <li><b>Data Integrity:</b> Validações de constraints e segurança de dados.</li>
  <li><b>CI/CD Ready:</b> Preparado para inspeção contínua e pipelines de deploy.</li>
</ul>

<hr>

<h2>🚦 Como Rodar Localmente</h2>
<ol>
  <li>Clone o repositório: <code>git clone https://github.com/Ja0Santana/usuario.git</code></li>
  <li>Configure o <b>PostgreSQL</b> no <code>application.properties</code> ou via Docker.</li>
  <li>Execute: <code>./gradlew bootRun</code></li>
</ol>

<hr>
