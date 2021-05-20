package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.collect.Multiset;
import edu.stanford.nlp.trees.GrammaticalStructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedWikiDocument {
  private String title;
  private List<Sentence> sentences;
  private DocumentMetaLib lib;
  private Multiset<String> identifier;

  public ParsedWikiDocument() {
  }

  public ParsedWikiDocument(String title, List<Sentence> sentences, DocumentMetaLib lib) {
    this.title = title;
    this.lib = lib;
    this.sentences = sentences;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setSentences(List<Sentence> sentences) {
    this.sentences = sentences;
  }

  public void setLib(DocumentMetaLib lib) {
    this.lib = lib;
  }

  public void setIdentifier(Multiset<String> identifier) {
    this.identifier = identifier;
  }

  public String getTitle() {
    return title;
  }

  public List<Sentence> getSentences() {
    return sentences;
  }

  public DocumentMetaLib getLib() {
    return lib;
  }

  public MathTagGraph getFormulaGraph() {
    return lib.getGraph();
  }

  public Collection<MathTag> getFormulae() {
    return lib.getFormulaLib().values();
  }

  public Map<String, MathTag> getFormulaeMap() {
    return lib.getFormulaLib();
  }

  public Map<String, SpecialToken> getCitationMap() {
    return lib.getCiteLib();
  }

  public Map<String, SpecialToken> getLinkMap() {
    return lib.getLinkLib();
  }

  public Multiset<String> getIdentifier() {
    return identifier;
  }

  /**
   * @deprecated we switching to MOI instead of single identifiers.
   */
  @Deprecated
  public void setIdentifiers(Multiset<String> identifier) {
    this.identifier = identifier;
  }

  /**
   * @deprecated we switching to MOI instead of single identifiers.
   */
  @Deprecated
  public Multiset<String> getIdentifiers() {
    return this.identifier;
  }
}
