package com.tw.controller;

import com.tw.model.Mensaje;
import com.tw.service.MensajeService;
import com.tw.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;
    private final UsuarioService usuarioService;

    public MensajeController(MensajeService mensajeService, UsuarioService usuarioService) {
        this.mensajeService = mensajeService;
        this.usuarioService = usuarioService;
    }

    // ---- Bandeja de entrada ----
    @GetMapping
    public String bandeja(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam(required = false, defaultValue = "entrada") String tab,
                          Model model) {
        String email = userDetails.getUsername();
        List<Mensaje> entrada = mensajeService.getBandejaEntrada(email);
        List<Mensaje> enviados = mensajeService.getEnviados(email);

        model.addAttribute("entrada", entrada);
        model.addAttribute("enviados", enviados);
        model.addAttribute("tab", tab);
        model.addAttribute("noLeidos", mensajeService.countNoLeidos(email));
        return "mensajes/bandeja";
    }

    // ---- Ver mensaje ----
    @GetMapping("/{id}")
    public String verMensaje(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        Mensaje m = mensajeService.marcarLeido(id, userDetails.getUsername());
        model.addAttribute("mensaje", m);
        boolean esMio = m.getDestinatario().getEmail().equalsIgnoreCase(userDetails.getUsername());
        model.addAttribute("esDestinatario", esMio);
        return "mensajes/detalle-mensaje";
    }

    // ---- Formulario nuevo mensaje ----
    @GetMapping("/nuevo")
    public String nuevoForm(@RequestParam(required = false) String para,
                             Model model) {
        model.addAttribute("para", para != null ? para : "");
        return "mensajes/nuevo-mensaje";
    }

    // ---- Enviar mensaje ----
    @PostMapping("/enviar")
    public String enviar(@RequestParam String destinatario,
                          @RequestParam String asunto,
                          @RequestParam String contenido,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes flash) {
        try {
            mensajeService.enviar(userDetails.getUsername(), destinatario, asunto, contenido);
            flash.addFlashAttribute("exito", "Mensaje enviado correctamente");
            return "redirect:/mensajes?tab=enviados";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/mensajes/nuevo?para=" + destinatario;
        }
    }

    // ---- Eliminar mensaje ----
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(required = false, defaultValue = "entrada") String tab,
                            RedirectAttributes flash) {
        try {
            mensajeService.eliminarParaUsuario(id, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Mensaje eliminado");
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mensajes?tab=" + tab;
    }
}
