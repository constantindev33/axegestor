package com.ilefilhosdosol.axegestor.service;

import com.ilefilhosdosol.axegestor.dto.RelatorioFinanceiroMensalResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoMensalidadeMembroResponse;
import com.ilefilhosdosol.axegestor.dto.ResumoPorChaveResponse;
import com.ilefilhosdosol.axegestor.model.LancamentoFinanceiro;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class RelatorioArquivoService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] gerarExcel(RelatorioFinanceiroMensalResponse relatorio) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                adicionarEntrada(zip, "[Content_Types].xml", """
                        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                          <Default Extension="xml" ContentType="application/xml"/>
                          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                          <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                          <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                          <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                        </Types>
                        """);
                adicionarEntrada(zip, "_rels/.rels", """
                        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                        </Relationships>
                        """);
                adicionarEntrada(zip, "xl/_rels/workbook.xml.rels", """
                        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
                          <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
                          <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
                        </Relationships>
                        """);
                adicionarEntrada(zip, "xl/workbook.xml", """
                        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                          <sheets>
                            <sheet name="Resumo" sheetId="1" r:id="rId1"/>
                            <sheet name="Categorias" sheetId="2" r:id="rId2"/>
                            <sheet name="Lancamentos" sheetId="3" r:id="rId3"/>
                            <sheet name="Mensalidades" sheetId="4" r:id="rId4"/>
                          </sheets>
                        </workbook>
                        """);

                adicionarEntrada(zip, "xl/worksheets/sheet1.xml", planilha(linhasResumo(relatorio)));
                adicionarEntrada(zip, "xl/worksheets/sheet2.xml", planilha(linhasCategorias(relatorio)));
                adicionarEntrada(zip, "xl/worksheets/sheet3.xml", planilha(linhasLancamentos(relatorio.lancamentos())));
                adicionarEntrada(zip, "xl/worksheets/sheet4.xml", planilha(linhasMensalidades(relatorio.mensalidades())));
            }

            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gerar o arquivo Excel", exception);
        }
    }

    public byte[] gerarPdf(RelatorioFinanceiroMensalResponse relatorio) {
        List<String> linhas = new ArrayList<>();
        linhas.add("AXEGESTOR - PRESTACAO DE CONTAS");
        linhas.add("Periodo: " + relatorio.mes() + "/" + relatorio.ano());
        linhas.add("");
        linhas.add("Resumo financeiro");
        linhas.add("Receitas: " + dinheiro(relatorio.receitas()));
        linhas.add("Despesas: " + dinheiro(relatorio.despesas()));
        linhas.add("Saldo: " + dinheiro(relatorio.saldo()));
        linhas.add("Lancamentos: " + relatorio.totalLancamentos());
        linhas.add("Pagos: " + relatorio.totalPagos() + " | Pendentes: " + relatorio.totalPendentes() + " | Atrasados: " + relatorio.totalAtrasados());
        linhas.add("");
        linhas.add("Mensalidades");
        linhas.add("Em dia: " + relatorio.mensalidadesEmDia());
        linhas.add("Atrasadas: " + relatorio.mensalidadesAtrasadas());
        linhas.add("Sem cadastro: " + relatorio.mensalidadesSemCadastro());
        linhas.add("");
        linhas.add("Totais por categoria");
        relatorio.porCategoria().stream().limit(10).forEach(item ->
                linhas.add(formatarChave(item.chave()) + " - " + item.quantidade() + " lanc. - " + dinheiro(item.total()))
        );
        linhas.add("");
        linhas.add("Mensalidades criticas");
        relatorio.mensalidades().stream()
                .filter(item -> List.of("ATRASADA", "PENDENTE", "SEM_MENSALIDADE").contains(item.statusMensalidade()))
                .limit(12)
                .forEach(item -> linhas.add(item.nome() + " - " + item.statusMensalidade() + " - " + dinheiro(item.valor())));

        String conteudo = montarConteudoPdf(linhas);
        return montarPdf(conteudo);
    }

    private List<List<String>> linhasResumo(RelatorioFinanceiroMensalResponse relatorio) {
        return List.of(
                List.of("Indicador", "Valor"),
                List.of("Periodo", relatorio.mes() + "/" + relatorio.ano()),
                List.of("Receitas", dinheiro(relatorio.receitas())),
                List.of("Despesas", dinheiro(relatorio.despesas())),
                List.of("Saldo", dinheiro(relatorio.saldo())),
                List.of("Total de lancamentos", String.valueOf(relatorio.totalLancamentos())),
                List.of("Pagos", String.valueOf(relatorio.totalPagos())),
                List.of("Pendentes", String.valueOf(relatorio.totalPendentes())),
                List.of("Atrasados", String.valueOf(relatorio.totalAtrasados())),
                List.of("Mensalidades em dia", String.valueOf(relatorio.mensalidadesEmDia())),
                List.of("Mensalidades atrasadas", String.valueOf(relatorio.mensalidadesAtrasadas())),
                List.of("Mensalidades sem cadastro", String.valueOf(relatorio.mensalidadesSemCadastro()))
        );
    }

    private List<List<String>> linhasCategorias(RelatorioFinanceiroMensalResponse relatorio) {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(List.of("Tipo", "Chave", "Quantidade", "Total"));
        relatorio.porCategoria().forEach(item -> linhas.add(linhaResumo("Categoria", item)));
        relatorio.porStatus().forEach(item -> linhas.add(linhaResumo("Status", item)));
        return linhas;
    }

    private List<String> linhaResumo(String tipo, ResumoPorChaveResponse item) {
        return List.of(tipo, formatarChave(item.chave()), String.valueOf(item.quantidade()), dinheiro(item.total()));
    }

    private List<List<String>> linhasLancamentos(List<LancamentoFinanceiro> lancamentos) {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(List.of("Descricao", "Responsavel", "Valor", "Data", "Vencimento", "Pagamento", "Tipo", "Categoria", "Status"));
        lancamentos.forEach(item -> linhas.add(List.of(
                nulo(item.getDescricao()),
                nulo(item.getResponsavel()),
                dinheiro(item.getValor()),
                item.getDataLancamento() != null ? item.getDataLancamento().format(DATA_BR) : "",
                item.getDataVencimento() != null ? item.getDataVencimento().format(DATA_BR) : "",
                item.getDataPagamento() != null ? item.getDataPagamento().format(DATA_BR) : "",
                item.getTipo() != null ? item.getTipo().name() : "",
                item.getCategoria() != null ? item.getCategoria().name() : "",
                item.getStatus() != null ? item.getStatus().name() : ""
        )));
        return linhas;
    }

    private List<List<String>> linhasMensalidades(List<ResumoMensalidadeMembroResponse> mensalidades) {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(List.of("Membro", "Telefone", "Email", "Status membro", "Status mensalidade", "Valor", "Vencimento", "Pagamento"));
        mensalidades.forEach(item -> linhas.add(List.of(
                nulo(item.nome()),
                nulo(item.telefone()),
                nulo(item.email()),
                nulo(item.statusMembro()),
                nulo(item.statusMensalidade()),
                dinheiro(item.valor()),
                item.dataVencimento() != null ? item.dataVencimento().format(DATA_BR) : "",
                item.dataPagamento() != null ? item.dataPagamento().format(DATA_BR) : ""
        )));
        return linhas;
    }

    private String planilha(List<List<String>> linhas) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int rowIndex = 0; rowIndex < linhas.size(); rowIndex++) {
            xml.append("<row r=\"").append(rowIndex + 1).append("\">");
            List<String> linha = linhas.get(rowIndex);
            for (int colIndex = 0; colIndex < linha.size(); colIndex++) {
                xml.append("<c r=\"").append(coluna(colIndex)).append(rowIndex + 1).append("\" t=\"inlineStr\"><is><t>")
                        .append(xml(linha.get(colIndex)))
                        .append("</t></is></c>");
            }
            xml.append("</row>");
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private void adicionarEntrada(ZipOutputStream zip, String nome, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(nome));
        zip.write(conteudo.stripLeading().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String montarConteudoPdf(List<String> linhas) {
        StringBuilder conteudo = new StringBuilder("BT\n/F1 11 Tf\n50 790 Td\n14 TL\n");
        for (String linha : linhas) {
            conteudo.append("(").append(pdf(linha)).append(") Tj\nT*\n");
        }
        conteudo.append("ET\n");
        return conteudo.toString();
    }

    private byte[] montarPdf(String conteudo) {
        byte[] stream = conteudo.getBytes(StandardCharsets.ISO_8859_1);
        List<String> objetos = new ArrayList<>();
        objetos.add("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
        objetos.add("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
        objetos.add("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
        objetos.add("4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
        objetos.add("5 0 obj << /Length " + stream.length + " >> stream\n" + conteudo + "endstream\nendobj\n");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (String objeto : objetos) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length);
            pdf.append(objeto);
        }
        int xref = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
        pdf.append("xref\n0 ").append(objetos.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer << /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private String coluna(int indice) {
        StringBuilder coluna = new StringBuilder();
        int valor = indice + 1;
        while (valor > 0) {
            int resto = (valor - 1) % 26;
            coluna.insert(0, (char) ('A' + resto));
            valor = (valor - 1) / 26;
        }
        return coluna.toString();
    }

    private String dinheiro(BigDecimal valor) {
        return valor != null ? "R$ " + valor.toString().replace(".", ",") : "";
    }

    private String nulo(String valor) {
        return valor != null ? valor : "";
    }

    private String formatarChave(String valor) {
        return valor == null ? "" : valor.toLowerCase().replace("_", " ");
    }

    private String xml(String valor) {
        return nulo(valor)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String pdf(String valor) {
        String semAcentos = Normalizer.normalize(nulo(valor), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
