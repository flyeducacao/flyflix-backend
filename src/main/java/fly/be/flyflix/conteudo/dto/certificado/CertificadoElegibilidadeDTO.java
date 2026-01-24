
package fly.be.flyflix.conteudo.dto.certificado;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CertificadoElegibilidadeDTO {
    private boolean elegivel;
    private String motivo;
    private long aulasTotais;
    private long aulasAssistidas;
}
