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
	public void testCalcularCustoTotal_CarrinhoComDesconto20Porcento_ComTotalProdutosMaiorQue1000() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(600), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(600), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 1200, desconto de 20% aplicado = 960, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(960.0).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado, custoTotal);
	}


	@Test
	public void testCalcularCustoTotal_CarrinhoComDesconto20Porcento_ComTotalProdutosExatamente1000() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(500), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(500), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 1000, desconto de 20% aplicado = 800, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(800.0).add(BigDecimal.valueOf(120.0)); // Valor corrigido
		assertEquals(esperado, custoTotal);
	}




	@Test
	public void testCalcularCustoTotal_CarrinhoComDesconto20Porcento_ComTotalProdutosMaiorQue1000_Exato() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(600), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(601), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 1201, desconto de 20% aplicado = 960.8, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(960.8).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado.setScale(2, BigDecimal.ROUND_HALF_UP), custoTotal.setScale(2, BigDecimal.ROUND_HALF_UP));
	}




	@Test
	public void testCalcularCustoTotal_CarrinhoComDesconto10Porcento_ComTotalProdutosMaiorQue500MenorQue100() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(400), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(200), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 600, desconto de 10% aplicado = 540, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(540.0).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado, custoTotal);
	}

	@Test
	public void testCalcularCustoTotal_ComTotalProdutosExatamente500() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(250), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(250), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 500, desconto de 10% aplicado = 450, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(450.0).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado, custoTotal);
	}

	@Test
	public void testCalcularCustoTotal_ComTotalProdutosExatamente501() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(250), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(251), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 501, desconto de 10% aplicado = 450.9, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(450.9).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado, custoTotal);
	}




	@Test
	public void testCalcularCustoTotal_CarrinhoComDesconto10Porcento_ComTotalProdutosMaiorQue500_Exato() {
		Cliente cliente = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(400), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(101), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, List.of(item1, item2), null);

		BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

		// Produtos custam 501, desconto de 10% aplicado = 450.9, frete = 120 (peso 30 kg)
		BigDecimal esperado = BigDecimal.valueOf(450.9).add(BigDecimal.valueOf(120.0));
		assertEquals(esperado.setScale(2, BigDecimal.ROUND_HALF_UP), custoTotal.setScale(2, BigDecimal.ROUND_HALF_UP));
	}




	@Test
	public void testCalcularCustoTotal_ComFreteCorretoParaTipoDeCliente() {
		// Cliente BRONZE, frete não é gratuito e tem custo normal
		Cliente clienteBronze = new Cliente(1L, "João", "Endereço", TipoCliente.BRONZE);
		// Cliente OURO, frete é gratuito
		Cliente clienteOuro = new Cliente(2L, "Maria", "Endereço", TipoCliente.OURO);

		Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(300), 10, TipoProduto.ELETRONICO);
		Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(280), 20, TipoProduto.LIVRO);

		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 1L);

		// Testando com cliente BRONZE
		CarrinhoDeCompras carrinhoBronze = new CarrinhoDeCompras(1L, clienteBronze, List.of(item1, item2), null);
		BigDecimal custoTotalBronze = compraService.calcularCustoTotal(carrinhoBronze);

		// Testando com cliente OURO
		CarrinhoDeCompras carrinhoOuro = new CarrinhoDeCompras(2L, clienteOuro, List.of(item1, item2), null);
		BigDecimal custoTotalOuro = compraService.calcularCustoTotal(carrinhoOuro);

		// Preço dos produtos sem desconto
		BigDecimal precoProdutos = BigDecimal.valueOf(580.0);

		// Para cliente BRONZE, o frete será de 120.0 (peso 30 kg * 4 reais), e o desconto de 10% aplica em 580.0 = 522.0
		BigDecimal esperadoBronze = precoProdutos.multiply(BigDecimal.valueOf(0.9)).add(BigDecimal.valueOf(120.0));

		// Para cliente OURO, o frete será 0 (frete grátis), e o desconto de 10% aplica em 580.0 = 522.0
		BigDecimal esperadoOuro = precoProdutos.multiply(BigDecimal.valueOf(0.9));

		// Arredondar os valores para 2 casas decimais antes de comparar
		assertEquals(esperadoBronze.setScale(2, BigDecimal.ROUND_HALF_UP), custoTotalBronze.setScale(2, BigDecimal.ROUND_HALF_UP));
		assertEquals(esperadoOuro.setScale(2, BigDecimal.ROUND_HALF_UP), custoTotalOuro.setScale(2, BigDecimal.ROUND_HALF_UP));
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



	@Test
	void testFinalizarCompra_CarrinhoNull() {
		// Dado que o carrinho é null
		Long clienteId = 1L;
		Long carrinhoId = 1L;

		// Simula a busca do cliente
		Cliente cliente = new Cliente();
		when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);

		// Quando o carrinho não é encontrado, retornando null
		when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(null);

		// Então uma IllegalStateException deve ser lançada
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		assertEquals("Carrinho vazio ou não encontrado.", exception.getMessage());
	}

	@Test
	void testFinalizarCompra_CarrinhoItensForNull() {
		// Dado que o carrinho tem itens = null
		Long clienteId = 1L;
		Long carrinhoId = 1L;

		// Simula a busca do cliente
		Cliente cliente = new Cliente();
		when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);

		// Simula o carrinho com itens == null usando mock
		CarrinhoDeCompras carrinho = mock(CarrinhoDeCompras.class);
		carrinho.setCliente(cliente); // Supondo que o método setCliente é simples e não precise de mock
		when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
		when(carrinho.getItens()).thenReturn(null);

		// Então uma IllegalStateException deve ser lançada
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		assertEquals("Carrinho vazio ou não encontrado.", exception.getMessage());
	}


	@Test
	void testFinalizarCompra_CarrinhoSemCliente() {
		// Dado que o carrinho não está associado a um cliente
		Long clienteId = 1L;
		Long carrinhoId = 1L;

		// Simula a busca do cliente (o cliente existe)
		Cliente cliente = new Cliente();
		when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);

		// Simula o carrinho com cliente == null
		CarrinhoDeCompras carrinho = mock(CarrinhoDeCompras.class);
		when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
		when(carrinho.getItens()).thenReturn(Collections.singletonList(new ItemCompra())); // Adiciona um item no carrinho
		when(carrinho.getCliente()).thenReturn(null); // Carrinho sem cliente associado

		// Então uma IllegalStateException deve ser lançada
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		assertEquals("Carrinho não está associado a um cliente válido.", exception.getMessage());
	}


	@Test
	public void calcularFrete_ComPesoIgual50_DeveCalcularComValor4PorPeso() {
		int pesoTotal = 50;  // Peso igual a 50 (limite)
		TipoCliente tipoCliente = TipoCliente.BRONZE;
		BigDecimal freteEsperado = BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(pesoTotal));  // Esperado: 4 * 50

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser calculado com 4 reais por peso para peso igual a 50.");
	}

	@Test
	public void calcularFrete_ComPesoMaiorQue50_DeveCalcularComValor7PorPeso() {
		int pesoTotal = 51;  // Peso acima de 50
		TipoCliente tipoCliente = TipoCliente.BRONZE;
		BigDecimal freteEsperado = BigDecimal.valueOf(7).multiply(BigDecimal.valueOf(pesoTotal));  // Esperado: 7 * 51

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser calculado com 7 reais por peso para peso maior que 50.");
	}

	@Test
	public void calcularFrete_ComPesoMenorQue50_DeveCalcularComValor4PorPeso() {
		int pesoTotal = 49;  // Peso abaixo de 50
		TipoCliente tipoCliente = TipoCliente.BRONZE;
		BigDecimal freteEsperado = BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(pesoTotal));  // Esperado: 4 * 49

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser calculado com 4 reais por peso para peso menor que 50.");
	}

	@Test
	public void calcularFrete_ComPesoIgual5_DeveCalcularComValorZero() {
		int pesoTotal = 5;  // Peso igual a 5
		TipoCliente tipoCliente = TipoCliente.BRONZE;
		BigDecimal freteEsperado = BigDecimal.ZERO;  // Esperado: zero para peso igual a 5

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser zero para peso igual a 5.");
	}

	@Test
	public void calcularFrete_ComPesoMaiorQue5_DeveCalcularComValor2PorPeso() {
		int pesoTotal = 6;  // Peso maior que 5
		TipoCliente tipoCliente = TipoCliente.BRONZE;
		BigDecimal freteEsperado = BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(pesoTotal));  // Esperado: 2 * 6

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser calculado com 2 reais por peso para peso maior que 5.");
	}

	@Test
	public void calcularFrete_ComPesoMenorQue5_DeveRetornarZero() {
		int pesoTotal = 4;  // Peso abaixo de 5
		TipoCliente tipoCliente = TipoCliente.BRONZE;
		BigDecimal freteEsperado = BigDecimal.ZERO;  // Frete esperado: zero para peso abaixo de 5

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser zero para peso abaixo de 5.");
	}

	@Test
	public void calcularFrete_ComClienteTipoOuro_DeveAplicarIsencaoDeFrete() {
		int pesoTotal = 30;  // Peso qualquer
		TipoCliente tipoCliente = TipoCliente.OURO;  // Cliente tipo OURO (isenção de frete)
		BigDecimal freteEsperado = BigDecimal.ZERO;  // Esperado: zero (isenção de frete)

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser isento para clientes do tipo OURO.");
	}

	@Test
	public void calcularFrete_ComClienteTipoPrata_DeveAplicarDescontoDe50PorcentoNoFrete() {
		int pesoTotal = 20;  // Peso entre 10 e 50
		TipoCliente tipoCliente = TipoCliente.PRATA;  // Cliente tipo PRATA
		BigDecimal custoFrete = BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(pesoTotal));  // Esperado: 4 * 20
		BigDecimal freteEsperado = custoFrete.multiply(BigDecimal.valueOf(0.5));  // Esperado: 50% de desconto

		BigDecimal resultado = compraService.calcularFrete(pesoTotal, tipoCliente);

		assertEquals(freteEsperado, resultado, "O frete deve ser 50% mais barato para clientes do tipo PRATA.");
	}




}
