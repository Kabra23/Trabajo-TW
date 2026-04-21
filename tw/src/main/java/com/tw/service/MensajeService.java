package com.tw.service;

import com.tw.model.Mensaje;
import com.tw.model.Usuario;
import com.tw.repository.MensajeRepository;
import com.tw.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MensajeService {

    private final MensajeRepository mensajeRepo;
    private final UsuarioRepository usuarioRepo;

    public MensajeService(MensajeRepository mensajeRepo, UsuarioRepository usuarioRepo) {
        this.mensajeRepo = mensajeRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // ---- Lectura ----

    @Transactional(readOnly = true)
    public List<Mensaje> getBandejaEntrada(String email) {
        Usuario u = obtenerUsuario(email);
        return mensajeRepo.findBandejaEntrada(u.getId());
    }

    @Transactional(readOnly = true)
    public List<Mensaje> getEnviados(String email) {
        Usuario u = obtenerUsuario(email);
        return mensajeRepo.findEnviados(u.getId());
    }

    @Transactional(readOnly = true)
    public Mensaje buscarPorId(Long id) {
        return mensajeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
    }

    @Transactional(readOnly = true)
    public long countNoLeidos(String email) {
        Usuario u = obtenerUsuario(email);
        return mensajeRepo.countNoLeidos(u.getId());
    }

    // ---- Escritura ----

    public Mensaje enviar(String emailRemitente, String emailDestinatario,
                          String asunto, String contenido) {
        if (emailRemitente.equalsIgnoreCase(emailDestinatario)) {
            throw new IllegalArgumentException("No puedes enviarte un mensaje a ti mismo");
        }

        Usuario remitente = obtenerUsuario(emailRemitente);
        Usuario destinatario = usuarioRepo.findByEmail(emailDestinatario)
                .orElseThrow(() -> new IllegalArgumentException("No existe ninguna cuenta con ese email"));

        Mensaje m = Mensaje.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .asunto(asunto != null && !asunto.isBlank() ? asunto.trim() : "(Sin asunto)")
                .contenido(contenido.trim())
                .build();

        return mensajeRepo.save(m);
    }

    /** Marca el mensaje como leido si el usuario es el destinatario */
    public Mensaje marcarLeido(Long mensajeId, String email) {
        Mensaje m = buscarPorId(mensajeId);
        verificarAcceso(m, email);
        if (m.getDestinatario().getEmail().equalsIgnoreCase(email)) {
            m.setLeido(true);
            mensajeRepo.save(m);
        }
        return m;
    }

    /** Elimina el mensaje para el usuario que lo solicita */
    public void eliminarParaUsuario(Long mensajeId, String email) {
        Mensaje m = buscarPorId(mensajeId);
        if (m.getDestinatario().getEmail().equalsIgnoreCase(email)) {
            m.setEliminadoDestinatario(true);
        } else if (m.getRemitente().getEmail().equalsIgnoreCase(email)) {
            m.setEliminadoRemitente(true);
        } else {
            throw new SecurityException("No tienes permiso para eliminar este mensaje");
        }
        // Si ambos lo han eliminado, borrar definitivamente
        if (Boolean.TRUE.equals(m.getEliminadoDestinatario())
                && Boolean.TRUE.equals(m.getEliminadoRemitente())) {
            mensajeRepo.delete(m);
        } else {
            mensajeRepo.save(m);
        }
    }

    // ---- Privados ----

    private Usuario obtenerUsuario(String email) {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void verificarAcceso(Mensaje m, String email) {
        boolean esRemitente = m.getRemitente().getEmail().equalsIgnoreCase(email);
        boolean esDestinatario = m.getDestinatario().getEmail().equalsIgnoreCase(email);
        if (!esRemitente && !esDestinatario) {
            throw new SecurityException("No tienes permiso para acceder a este mensaje");
        }
    }
}
