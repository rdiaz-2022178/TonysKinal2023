package org.rodrigodiaz.controller;

import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import java.awt.Toolkit;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.JOptionPane;
import org.rodrigodiaz.bean.Plato;
import org.rodrigodiaz.bean.Producto;
import org.rodrigodiaz.bean.ProductosPlatos;
import org.rodrigodiaz.db.Conexion;
import org.rodrigodiaz.main.Principal;

public class ProductoPlatoController implements Initializable {

    private Principal escenarioPrincipal;

    private enum operaciones {
        GUARDAR, ACTUALIZAR, ELIMINAR, NINGUNO
    };
    private operaciones tipoDeOperacion = operaciones.NINGUNO;
    private ObservableList<ProductosPlatos> listaProductosPlatos;
    private ObservableList<Producto> listaProductos;
    private ObservableList<Plato> listaPlatos;

    @FXML
    private ImageView imgNuevo;
    @FXML
    private ImageView imgEditar;
    @FXML
    private ImageView imgEliminar;
    @FXML
    private ImageView imgReporte;
    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnReporte;
    @FXML
    private TextField txtCodigoProductoPlato;
    @FXML
    private ComboBox cbxCodigoProducto;
    @FXML
    private ComboBox cbxCodigoPlato;
    @FXML
    private TableView tblProductoPlatos;
    @FXML
    private TableColumn colCodigoProductoPlato;
    @FXML
    private TableColumn colCodigoProducto;
    @FXML
    private TableColumn colCodigoPlato;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatos();
        cbxCodigoPlato.setDisable(true);
        cbxCodigoProducto.setDisable(true);
        cbxCodigoPlato.setItems(getPlatos());
        cbxCodigoProducto.setItems(getProducto());
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    public ObservableList<Producto> getProducto() {
        ArrayList<Producto> lista = new ArrayList<Producto>();
        try {
            PreparedStatement procedimiento = Conexion.getInstancia().getConexion().prepareCall("call sp_ListarProductos");
            ResultSet resultado = procedimiento.executeQuery();
            while (resultado.next()) {
                lista.add(new Producto(resultado.getInt("codigoProducto"),
                        resultado.getString("nombreProducto"),
                        resultado.getInt("cantidadProducto")));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return listaProductos = FXCollections.observableArrayList(lista);
    }

    public ObservableList<Plato> getPlatos() {
        ArrayList<Plato> lista = new ArrayList<Plato>();
        try {
            PreparedStatement procedimiento = Conexion.getInstancia().getConexion().prepareCall("call sp_ListarPlatos");
            ResultSet resultado = procedimiento.executeQuery();
            while (resultado.next()) {
                lista.add(new Plato(
                        resultado.getInt("codigoPlato"),
                        resultado.getInt("cantidad"),
                        resultado.getString("nombrePlato"),
                        resultado.getString("descripcionPlato"),
                        resultado.getDouble("precioPlato"),
                        resultado.getInt("codigoTipoPlato")));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return listaPlatos = FXCollections.observableArrayList(lista);
    }

    public ObservableList<ProductosPlatos> getProductoPlatos() {
        ArrayList<ProductosPlatos> lista = new ArrayList<ProductosPlatos>();
        try {
            PreparedStatement procedimiento = Conexion.getInstancia().getConexion().prepareCall("call sp_ListarProductos_has_Platos");
            ResultSet resultado = procedimiento.executeQuery();
            while (resultado.next()) {
                lista.add(new ProductosPlatos(
                        resultado.getInt("Productos_codigoProducto"),
                        resultado.getInt("codigoProducto"),
                        resultado.getInt("codigoPlato")));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return listaProductosPlatos = FXCollections.observableArrayList(lista);
    }

    public void cargarDatos() {
        tblProductoPlatos.setItems(getProductoPlatos());
        colCodigoProductoPlato.setCellValueFactory(new PropertyValueFactory<ProductosPlatos, Integer>("Productos_codigoProducto"));
        colCodigoProducto.setCellValueFactory(new PropertyValueFactory<ProductosPlatos, Integer>("codigoProducto"));
        colCodigoPlato.setCellValueFactory(new PropertyValueFactory<ProductosPlatos, Integer>("codigoPlato"));
    }

    public void seleccionarElemento() {
        if (tipoDeOperacion == operaciones.GUARDAR) {
            JOptionPane.showMessageDialog(null, "No puedes seleccionar elementos", "AVISO", JOptionPane.WARNING_MESSAGE);
        } else {
            if (tblProductoPlatos.getSelectionModel().getSelectedItem() == null) {
                JOptionPane.showMessageDialog(null, "Seleccione una fila correcta", "AVISO", JOptionPane.WARNING_MESSAGE);
            } else {
                txtCodigoProductoPlato.setText(String.valueOf(((ProductosPlatos) tblProductoPlatos.getSelectionModel().getSelectedItem()).getProductos_codigoProducto()));
                cbxCodigoPlato.getSelectionModel().select(buscarPlato(((ProductosPlatos) tblProductoPlatos.getSelectionModel().getSelectedItem()).getCodigoPlato()));
                cbxCodigoProducto.getSelectionModel().select(buscarProducto(((ProductosPlatos) tblProductoPlatos.getSelectionModel().getSelectedItem()).getCodigoProducto()));

            }
        }
    }

    public Plato buscarPlato(int codigoPlato) {
        Plato resultado = null;
        try {
            PreparedStatement procedimiento = Conexion.getInstancia().getConexion().prepareCall("call sp_BuscarPlato(?)");
            procedimiento.setInt(1, codigoPlato);
            ResultSet registro = procedimiento.executeQuery();
            while (registro.next()) {
                resultado = new Plato(registro.getInt("codigoPlato"),
                        registro.getInt("cantidad"),
                        registro.getString("nombrePlato"),
                        registro.getString("descripcionPlato"),
                        registro.getDouble("precioPlato"),
                        registro.getInt("codigoTipoPlato"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public Producto buscarProducto(int codigoProducto) {
        Producto resultado = null;
        try {
            PreparedStatement procedimiento = Conexion.getInstancia().getConexion().prepareCall("call sp_BuscarProducto(?)");
            procedimiento.setInt(1, codigoProducto);
            ResultSet registro = procedimiento.executeQuery();
            while (registro.next()) {
                resultado = new Producto(registro.getInt("codigoProducto"),
                        registro.getString("nombreProducto"),
                        registro.getInt("cantidadProducto"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public void nuevo() {
        switch (tipoDeOperacion) {
            case NINGUNO:
                limpiarControles();
                activarControles();
                btnNuevo.setText("GUARDAR     ");
                btnReporte.setText("CANCELAR     ");
                btnEditar.setDisable(true);
                btnReporte.setDisable(false);
                imgNuevo.setImage(new Image("/org/rodrigodiaz/image/iconoGuardar.png"));
                imgReporte.setImage(new Image("/org/rodrigodiaz/image/iconoCancelar.png"));
                tipoDeOperacion = operaciones.GUARDAR;
                break;
            case GUARDAR:
                guardar();
                break;
        }
    }

    public void guardar() {
        ProductosPlatos registro = new ProductosPlatos();
        try {
            if (cbxCodigoPlato.getSelectionModel().isEmpty() || cbxCodigoProducto.getSelectionModel().isEmpty() || txtCodigoProductoPlato.getText().trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Debe de rellenar todos los campos", "AVISO", JOptionPane.WARNING_MESSAGE);
            } else {
                registro.setProductos_codigoProducto(Integer.parseInt(txtCodigoProductoPlato.getText().trim()));
                registro.setCodigoProducto(((Producto) cbxCodigoProducto.getSelectionModel().getSelectedItem()).getCodigoProducto());
                registro.setCodigoPlato(((Plato) cbxCodigoPlato.getSelectionModel().getSelectedItem()).getCodigoPlato());
                PreparedStatement procedimiento = Conexion.getInstancia().getConexion().prepareCall("call sp_AgregarProducto_has_Plato(?,?,?)");
                procedimiento.setInt(1, registro.getProductos_codigoProducto());
                procedimiento.setInt(2, registro.getCodigoProducto());
                procedimiento.setInt(3, registro.getCodigoPlato());
                procedimiento.execute();
                listaProductosPlatos.add(registro);
                postGuardar();
            }
        } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException error) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "El código ya existe", "AVISO", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Código incorrecto", "AVISO", JOptionPane.WARNING_MESSAGE);
        } catch (MysqlDataTruncation e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Verifique el número de carácteres", "AVISO", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void postGuardar() {
        limpiarControles();
        desactivarControles();
        btnNuevo.setText("NUEVO         ");
        btnReporte.setText("REPORTE     ");
        btnEditar.setDisable(true);
        btnReporte.setDisable(false);
        imgNuevo.setImage(new Image("/org/rodrigodiaz/image/iconoAgregar.png"));
        imgReporte.setImage(new Image("/org/rodrigodiaz/image/iconoReporte.png"));
        tipoDeOperacion = operaciones.NINGUNO;
        cargarDatos();
    }

    public void reporte() {
        switch (tipoDeOperacion) {
            case GUARDAR:
                limpiarControles();
                desactivarControles();
                btnNuevo.setText("NUEVO         ");
                btnReporte.setText("REPORTE     ");
                btnReporte.setDisable(false);
                imgNuevo.setImage(new Image("/org/rodrigodiaz/image/iconoAgregar.png"));
                imgReporte.setImage(new Image("/org/rodrigodiaz/image/iconoReporte.png"));
                tipoDeOperacion = operaciones.NINGUNO;
                break;
        }
    }

    public void limpiarControles() {
        txtCodigoProductoPlato.clear();
        cbxCodigoPlato.setValue(null);
        cbxCodigoProducto.setValue(null);

    }

    public void activarControles() {
        txtCodigoProductoPlato.setEditable(true);
        cbxCodigoPlato.setDisable(false);
        cbxCodigoProducto.setDisable(false);
    }

    public void desactivarControles() {
        txtCodigoProductoPlato.setEditable(false);
        cbxCodigoPlato.setDisable(true);
        cbxCodigoProducto.setDisable(true);
    }

    public Principal getEscenarioPrincipal() {
        return escenarioPrincipal;
    }

    public void setEscenarioPrincipal(Principal escenarioPrincipal) {
        this.escenarioPrincipal = escenarioPrincipal;
    }

    public void menuPrincipal() {
        escenarioPrincipal.menuPrincipal();
    }

}
