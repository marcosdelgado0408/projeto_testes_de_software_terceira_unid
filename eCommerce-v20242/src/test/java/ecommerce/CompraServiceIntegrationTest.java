package ecommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import ecommerce.entity.*;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;

public class CompraServiceIntegrationTest {

    @Mock
    private CarrinhoDeComprasService carrinhoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private IEstoqueExternal estoqueExternal;

    @Mock
    private IPagamentoExternal pagamentoExternal;

    @InjectMocks
    private CompraService compraService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFinalizarCompra_ComSucesso() {
        Long clienteId = 1L;
        Long carrinhoId = 1L;

        // Criando o cliente
        Cliente cliente = new Cliente(clienteId, "João", "Endereço", null);

        // Mock do carrinho de compras
        CarrinhoDeCompras carrinho = mock(CarrinhoDeCompras.class);

        // Configurando mocks
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);

        // Configurando cliente no carrinho
        when(carrinho.getCliente()).thenReturn(cliente);

        // Criando produto com o construtor completo
        Produto produto = new Produto(
                1L, // ID
                "Produto Teste", // Nome
                "Descrição do Produto", // Descrição
                BigDecimal.valueOf(100), // Preço
                500, // Peso
                TipoProduto.ELETRONICO // Tipo de Produto
        );

        // Criando item do carrinho
        ItemCompra item = new ItemCompra(1L, produto, 2L);

        // Configurando os itens no carrinho
        when(carrinho.getItens()).thenReturn(List.of(item));

        // Configurando disponibilidade no estoque
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, List.of()));

        // Simulando autorização de pagamento
        when(pagamentoExternal.autorizarPagamento(eq(clienteId), anyDouble()))
                .thenReturn(new PagamentoDTO(true, 123L));

        // Simulando baixa no estoque
        when(estoqueExternal.darBaixa(anyList(), anyList()))
                .thenReturn(new EstoqueBaixaDTO(true));

        // Chamando o método a ser testado
        CompraDTO compraDTO = compraService.finalizarCompra(carrinhoId, clienteId);

        // Verificações
        assertEquals(true, compraDTO.sucesso());
        assertEquals(123L, compraDTO.transacaoPagamentoId());
    }





    @Test
    public void testFinalizarCompra_FalhaNoPagamento() {
        Long clienteId = 2L;
        Long carrinhoId = 2L;

        Cliente cliente = new Cliente(clienteId, "Maria", "Endereço", null);
        CarrinhoDeCompras carrinho = mock(CarrinhoDeCompras.class);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);

        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(true, List.of()));

        when(pagamentoExternal.autorizarPagamento(eq(clienteId), anyDouble()))
                .thenReturn(new PagamentoDTO(false, null));

        assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(carrinhoId, clienteId);
        });

        verify(pagamentoExternal, never()).cancelarPagamento(anyLong(), anyLong());
    }

//    @Test
//    void testFinalizarCompra_ValidaPagamento() {
//        // Arrange
//        Cliente cliente = new Cliente();
//        cliente.setId(1L);
//        cliente.setTipo(TipoCliente.BRONZE);
//
//        Produto produto1 = new Produto();
//        produto1.setId(10L);
//        produto1.setPreco(BigDecimal.valueOf(100));
//        produto1.setPeso(5);
//
//        Produto produto2 = new Produto();
//        produto2.setId(20L);
//        produto2.setPreco(BigDecimal.valueOf(200));
//        produto2.setPeso(10);
//
//        ItemCompra item1 = new ItemCompra();
//        item1.setProduto(produto1);
//        item1.setQuantidade(2L);
//
//        ItemCompra item2 = new ItemCompra();
//        item2.setProduto(produto2);
//        item2.setQuantidade(1L);
//
//        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
//        carrinho.setCliente(cliente);
//        carrinho.setItens(Arrays.asList(item1, item2));
//
//        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
//        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);
//
//        // Disponibilidade retorna que todos os produtos estão disponíveis
//        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
//        when(estoqueExternal.verificarDisponibilidade(Arrays.asList(10L, 20L), Arrays.asList(2L, 1L)))
//                .thenReturn(disponibilidade);
//
//        // Configuração do pagamento autorizado
//        PagamentoDTO pagamento = new PagamentoDTO(true, 123L);
//        when(pagamentoExternal.autorizarPagamento(1L, 400.0)).thenReturn(pagamento);
//
//        // Act
//        CompraDTO resultado = compraService.finalizarCompra(1L, 1L);
//
//        // Assert
//        assertNotNull(resultado);
//        assertTrue(resultado.sucesso());
//        assertEquals(123L, resultado.transacaoPagamentoId());
//    }

    @Test
    void testFinalizarCompra_DisponibilidadeNull() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        // Configura o mock para retornar null
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L)))
                .thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }



    @Test
    void testFinalizarCompra_DisponibilidadeIndisponivel() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        // Configura o mock para retornar disponibilidade false
        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(false, List.of(10L));
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L)))
                .thenReturn(disponibilidade);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }



    @Test
    void testFinalizarCompra_BaixaEstoqueFalhaPagamentoCancelado() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        PagamentoDTO pagamento = new PagamentoDTO(true, 123L);
        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(pagamento);

        EstoqueBaixaDTO baixa = new EstoqueBaixaDTO(false);
        when(estoqueExternal.darBaixa(List.of(10L), List.of(1L))).thenReturn(baixa);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        // Verifica o cancelamento do pagamento
        verify(pagamentoExternal, times(1)).cancelarPagamento(1L, 123L);
        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
    }


    @Test
    void testFinalizarCompra_BaixaEstoqueFalhaSemPagamentoParaCancelar() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        PagamentoDTO pagamento = new PagamentoDTO(true, null);
        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(pagamento);

        EstoqueBaixaDTO baixa = new EstoqueBaixaDTO(false);
        when(estoqueExternal.darBaixa(List.of(10L), List.of(1L))).thenReturn(baixa);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        // Verifica que o pagamento não foi cancelado
        verify(pagamentoExternal, never()).cancelarPagamento(anyLong(), anyLong());
        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_BaixaEstoqueBemSucedida() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        PagamentoDTO pagamento = new PagamentoDTO(true, 123L);
        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(pagamento);

        EstoqueBaixaDTO baixa = new EstoqueBaixaDTO(true);
        when(estoqueExternal.darBaixa(List.of(10L), List.of(1L))).thenReturn(baixa);

        // Act
        CompraDTO resultado = compraService.finalizarCompra(1L, 1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.sucesso());
        assertEquals(123L, resultado.transacaoPagamentoId());

        // Verifica que o pagamento não foi cancelado
        verify(pagamentoExternal, never()).cancelarPagamento(anyLong(), anyLong());
    }


    @Test
    void testFinalizarCompra_PagamentoComTransacaoIdValido() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        PagamentoDTO pagamento = new PagamentoDTO(true, 123L); // Transação ID válido
        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(pagamento);

        EstoqueBaixaDTO baixa = new EstoqueBaixaDTO(false); // Simula falha no estoque
        when(estoqueExternal.darBaixa(List.of(10L), List.of(1L))).thenReturn(baixa);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        // Verifica que o cancelamento do pagamento foi chamado
        verify(pagamentoExternal, times(1)).cancelarPagamento(1L, 123L);

        // Verifica a mensagem da exceção
        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
    }


    @Test
    void testFinalizarCompra_PagamentoNull_LancaExcecao() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(null); // Pagamento é null

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Pagamento não autorizado.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_PagamentoNaoAutorizado_LancaExcecao() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        PagamentoDTO pagamento = new PagamentoDTO(false, null); // Pagamento não autorizado
        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(pagamento);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Pagamento não autorizado.", exception.getMessage());
    }

    @Test
    void testFinalizarCompra_PagamentoAutorizado_Sucesso() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setPeso(5);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(1L);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);

        DisponibilidadeDTO disponibilidade = new DisponibilidadeDTO(true, List.of());
        when(estoqueExternal.verificarDisponibilidade(List.of(10L), List.of(1L))).thenReturn(disponibilidade);

        PagamentoDTO pagamento = new PagamentoDTO(true, 123L); // Pagamento autorizado
        when(pagamentoExternal.autorizarPagamento(1L, 100.0)).thenReturn(pagamento);

        EstoqueBaixaDTO baixa = new EstoqueBaixaDTO(true); // Sucesso na baixa do estoque
        when(estoqueExternal.darBaixa(List.of(10L), List.of(1L))).thenReturn(baixa);

        // Act
        CompraDTO resultado = compraService.finalizarCompra(1L, 1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.sucesso());
        assertEquals("Compra finalizada com sucesso.", resultado.mensagem());
        assertEquals( 123L, resultado.transacaoPagamentoId());
    }







}
