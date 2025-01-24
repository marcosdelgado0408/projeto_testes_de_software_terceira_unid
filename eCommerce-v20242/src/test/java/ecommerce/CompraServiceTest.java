package ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecommerce.dto.CompraDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompraServiceTest {

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

	@Test
	public void testCalcularCustoTotal_CarrinhoComDesconto20Porcento() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(500), 10,
				TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(600), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 1100, desconto de 20% aplicado = 880, frete = 120 (peso 30
		// kg)
		BigDecimal esperado = BigDecimal.valueOf(880.0).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado, custoTotal);
	}

	@Test
	void testCalcularCustoTotal_ClienteOuroComFreteGratis() {
		// Configurar os itens no carrinho
		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(300), 2,
				TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(300), 3,
				TipoProduto.ELETRONICO);

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

	@Test
	public void testCalcularCustoTotal_CarrinhoComPesoExatoDe5kg() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(100), 5,
				TipoProduto.ELETRONICO);
		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Esperado: 100 (sem frete, pois o peso é 5kg)
		assertEquals(BigDecimal.valueOf(100), custoTotal);
	}

	@Test
	void testFinalizarCompra_CarrinhoVazio() {
		// Dado que o carrinho está vazio
		Long clienteId = 1L;
		Long carrinhoId = 1L;
		Cliente cliente = new Cliente();
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
		carrinho.setCliente(cliente);

		// Quando o carrinho é recuperado
		doReturn(new CarrinhoDeCompras()).when(carrinhoService).buscarPorCarrinhoIdEClienteId(1L, null);

		// Então uma IllegalStateException deve ser lançada
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		assertEquals("Carrinho vazio ou não encontrado.", exception.getMessage());
	}


}
