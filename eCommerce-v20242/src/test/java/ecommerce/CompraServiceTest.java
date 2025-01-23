package ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

public class CompraServiceTest {

    private CompraService compraService;

    @BeforeEach
    public void setup() {
        compraService = new CompraService(null, null, null, null);
    }

    @Test
    public void testCalcularCustoTotal_CarrinhoComDesconto20Porcento() {
        Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

        Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(500), 10, TipoProduto.ELETRONICO);
        Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(600), 20, TipoProduto.LIVRO);

        ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
        ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Produtos custam 1100, desconto de 20% aplicado = 880, frete = 120 (peso 30 kg)
        BigDecimal esperado = BigDecimal.valueOf(880.0).add(BigDecimal.valueOf(120.0));
        assertEquals(esperado, custoTotal);
    }

    @Test
    void testCalcularCustoTotal_ClienteOuroComFreteGratis() {
        // Configurar os itens no carrinho
        Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(300), 2, TipoProduto.ELETRONICO);
        Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(300), 3, TipoProduto.ELETRONICO);

        ItemCompra item1 = new ItemCompra(1L, produto1, 1L); // 300
        ItemCompra item2 = new ItemCompra(2L, produto2, 1L); // 300

        List<ItemCompra> itens = List.of(item1, item2);

        Cliente cliente = new Cliente(1L, "Cliente Ouro", "Endereço 1", TipoCliente.OURO);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, itens, LocalDate.now());

        // Chamar o método de cálculo
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Verificar o resultado (desconto de 10%, sem frete)
        BigDecimal expected = BigDecimal.valueOf(540.0); // 600 - 10%
        assertEquals(expected, custoTotal);
    }


    @Test
    public void testCalcularCustoTotal_SemDesconto() {
        Cliente cliente = new Cliente(3L, "Pedro", "Endereço", TipoCliente.BRONZE);

        Produto produto = new Produto(4L, "Produto 4", "Descrição 4", BigDecimal.valueOf(200), 5, TipoProduto.ALIMENTO);

        ItemCompra item = new ItemCompra(4L, produto, 2L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(3L, cliente, List.of(item), null);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Produtos custam 400, frete gratuito (peso total 10 kg)
        BigDecimal esperado = BigDecimal.valueOf(400).add(BigDecimal.valueOf(40)); // Frete: 10 kg * 4
        assertEquals(esperado, custoTotal);
    }
}
