package fly.be.flyflix.auth.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class FotoUploadDTO {
    @Schema(type = "string", format = "binary", description = "Imagem da foto de perfil (JPEG ou PNG)")
    private MultipartFile imagem;

    // getter e setter
    public MultipartFile getImagem() { return imagem; }
    public void setImagem(MultipartFile imagem) { this.imagem = imagem; }
}
