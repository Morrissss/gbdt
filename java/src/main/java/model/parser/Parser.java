package model.parser;

import model.Model;

import java.io.InputStream;

public interface Parser<T extends Model> {

    T parse(InputStream is);
}
