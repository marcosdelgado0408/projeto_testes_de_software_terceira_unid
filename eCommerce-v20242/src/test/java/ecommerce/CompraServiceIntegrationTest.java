package ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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


    @Test
    public void testFinalizarCompra_EstoqueIndisponivel() {
        Long clienteId = 2L;
        Long carrinhoId = 2L;

        Cliente cliente = new Cliente(clienteId, "Maria", "Endereço", null);
        Produto produto = new Produto(1L, "Produto Teste", "Descrição", BigDecimal.valueOf(100), 500, TipoProduto.ELETRONICO);
        ItemCompra item = new ItemCompra(1L, produto, 2L);
        CarrinhoDeCompras carrinho = mock(CarrinhoDeCompras.class);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        when(carrinho.getItens()).thenReturn(List.of(item));

        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
                .thenReturn(new DisponibilidadeDTO(false, List.of("Produto Teste")));

        assertThrows(IllegalStateException.class, () -> compraService.finalizarCompra(carrinhoId, clienteId));

        verify(pagamentoExternal, never()).autorizarPagamento(anyLong(), anyDouble());
    }

}
