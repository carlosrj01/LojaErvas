package main;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.effect.DropShadow;

public class MenuErvas extends Application {

    private Stage janelaPrincipal;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.janelaPrincipal = primaryStage;
        primaryStage.setTitle("Portuguesa Ervas");
        primaryStage.setResizable(false);

        // Fonte
        Font bigJohnFont = Font.font("Consolas", 18);

        // Painel de botões
        VBox painelBotoes = new VBox(10);
        painelBotoes.setAlignment(Pos.TOP_CENTER); // Começa no topo
        painelBotoes.setStyle("-fx-background-color: #ECEBD7;");

        // Botões com ícones e textos
        Button botaoCarrinho = criarBotao("Verificar Estoque", "file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/carrinho.png", bigJohnFont);
        Button botaoCadastro = criarBotao("Cadastro de Mercadoria", "file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/papel.png", bigJohnFont);
        Button botaoHistorico = criarBotao("Histórico de Saída", "file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/livro.png", bigJohnFont);
        Button botaoAlterarPreco = criarBotao("Alterar Preço", "file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/preco.png", bigJohnFont);
        Button botaoAtualizarEstoque = criarBotao("Adicionar Mercadoria", "file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/estoque.png", bigJohnFont);
        Button botaoRemoverEstoque = criarBotao("Remover Mercadoria", "file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/remover.png", bigJohnFont);

        // Ações dos botões para abrir novas janelas
        botaoCarrinho.setOnAction(e -> abrirCarrinhoDeCompras());
        botaoCadastro.setOnAction(e -> abrirCadastroProduto());
        botaoHistorico.setOnAction(e -> abrirHistoricoDeCompras());
        botaoAlterarPreco.setOnAction(e -> abrirModificarPrecoProduto()); 
        botaoAtualizarEstoque.setOnAction(e -> abrirAtualizarEstoque());
        botaoRemoverEstoque.setOnAction(e -> abrirRemoverEstoque());

        painelBotoes.getChildren().addAll(botaoAtualizarEstoque, botaoRemoverEstoque, botaoCarrinho, botaoCadastro, botaoHistorico, botaoAlterarPreco);

        // Imagem e título ao lado esquerdo
        ImageView imagemLogo = new ImageView(new Image("file:C:/Users/carlo/eclipse-workspace/LojaErvas/src/main/portervas.png"));
        imagemLogo.setFitWidth(200);
        imagemLogo.setPreserveRatio(true);

        Text titulo = new Text("\n\n  v.1.0.0 \n Bem-vindo");
        titulo.setFont(Font.font("Consolas", 20));
        titulo.setFill(Color.DARKGREEN);

        VBox painelLogoETitulo = new VBox(5, imagemLogo, titulo);
        painelLogoETitulo.setAlignment(Pos.TOP_CENTER);

        // Painel principal
        HBox painelPrincipal = new HBox(20);
        painelPrincipal.setAlignment(Pos.CENTER_LEFT);
        painelPrincipal.getChildren().addAll(painelLogoETitulo, painelBotoes);
        painelPrincipal.setStyle("-fx-background-color: #ECEBD7;");

        // Configuração da cena principal
        Scene scene = new Scene(painelPrincipal, 530, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Método para criar botões estilizados com sombreamento
    private Button criarBotao(String texto, String caminhoIcone, Font fonte) {
        Button botao = new Button(texto);
        botao.setFont(fonte);
        botao.setTextFill(Color.WHITE);
        botao.setStyle("-fx-background-color: #3A5A40; -fx-border-color: white; -fx-border-width: 2px;");
        botao.setPrefSize(300, 50);

        // Ícone do botão
        ImageView icone = new ImageView(new Image(caminhoIcone));
        icone.setFitWidth(30);
        icone.setFitHeight(30);
        botao.setGraphic(icone);

        // Adiciona sombreamento ao botão
        DropShadow sombra = new DropShadow();
        sombra.setRadius(5.0);
        sombra.setOffsetX(3.0);
        sombra.setOffsetY(3.0);
        sombra.setColor(Color.color(0.2, 0.2, 0.2));
        botao.setEffect(sombra);

        // Efeito de hover (passar o mouse por cima)
        botao.setOnMouseEntered(e -> botao.setStyle("-fx-background-color: #587A58;"));
        botao.setOnMouseExited(e -> botao.setStyle("-fx-background-color: #3A5A40;"));

        return botao;
    }

    private void abrirNovaJanela(String titulo) {
        Stage novaJanela = new Stage();
        novaJanela.setTitle(titulo);
        novaJanela.setOnCloseRequest((WindowEvent e) -> janelaPrincipal.show());

        janelaPrincipal.hide();

        VBox conteudo = new VBox();
        conteudo.setAlignment(Pos.CENTER);
        conteudo.getChildren().add(new Button("Aqui estará o conteúdo para " + titulo));
        Scene scene = new Scene(conteudo, 300, 200);

        novaJanela.setScene(scene);
        novaJanela.show();
    }

    private void abrirCadastroProduto() {
        Stage cadastroStage = new Stage();
        CadastroDeProduto cadastroDeProduto = new CadastroDeProduto(janelaPrincipal);
        cadastroDeProduto.start(cadastroStage);

        cadastroStage.setOnCloseRequest(e -> janelaPrincipal.show());
        janelaPrincipal.hide();
    }

    private void abrirCarrinhoDeCompras() {
        Stage carrinhoStage = new Stage();
        VerificaEstoque carrinhoDeCompras = new VerificaEstoque(janelaPrincipal);
        carrinhoDeCompras.start(carrinhoStage);

        carrinhoStage.setOnCloseRequest(e -> janelaPrincipal.show());
        janelaPrincipal.hide();
    }

    private void abrirHistoricoDeCompras() {
        Stage historicoStage = new Stage();
        HistoricoSaida historicoDeCompras = new HistoricoSaida(janelaPrincipal);
        historicoDeCompras.start(historicoStage);

        historicoStage.setOnCloseRequest(e -> janelaPrincipal.show());
        janelaPrincipal.hide();
    }

    private void abrirModificarPrecoProduto() {
        Stage modificarPrecoStage = new Stage();
        ModificarPrecoProduto modificarPrecoProduto = new ModificarPrecoProduto(janelaPrincipal);
        modificarPrecoProduto.start(modificarPrecoStage);

        modificarPrecoStage.setOnCloseRequest(e -> janelaPrincipal.show());
        janelaPrincipal.hide();
    }
    
    private void abrirAtualizarEstoque() {
        Stage atualizarEstoqueStage = new Stage();
        AdicionaEstoque atualizarEstoque = new AdicionaEstoque(janelaPrincipal);
        atualizarEstoque.start(atualizarEstoqueStage);
        janelaPrincipal.hide();
    }
    
    private void abrirRemoverEstoque() {
        Stage removerEstoqueStage = new Stage();
        RemoverEstoque removerEstoque = new RemoverEstoque(janelaPrincipal);
        removerEstoque.start(removerEstoqueStage);
        removerEstoqueStage.setOnCloseRequest(e -> janelaPrincipal.show());
        janelaPrincipal.hide();
    }
}
