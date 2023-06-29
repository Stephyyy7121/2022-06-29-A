package it.polito.tdp.itunes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.itunes.db.ItunesDAO;

public class Model {
	
	private ItunesDAO dao;
	
	private List<Album> allAlbum;
	private SimpleDirectedWeightedGraph<Album, DefaultWeightedEdge> graph ;
	
	//variabili globali
	private List<Album> bestPath;
	private int bestScore;
	private Album source;
	private Album target;
	
	public Model() {
		allAlbum = new ArrayList<>();
		this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		dao = new ItunesDAO();
		
	}
	
	
	public List<Album> getPath(Album source, Album target, int treshold) {
		
		/*List<Album> parziale = new ArrayList<Album>();
		this.bestPath = new ArrayList<>();
		this.bestScore = 0;
		
		parziale.add(source);
		
		ricorsione(parziale, target, treshold);
		
		return this.bestPath;*/
		
		this.source = source;
		this.target = target;
		this.bestPath = new ArrayList<>();
		this.bestScore = 0;
		
		List<Album> successori = Graphs.successorListOf(this.graph, source);
		List<Album> parziale = new ArrayList<>();
		parziale.add(source);
		
		ricorsione(parziale, successori, treshold);
		return this.bestPath;
		
	}
	
	private void ricorsione(List<Album> parziale, List<Album> successori, int treshold) {
		
		Album current = parziale.get(parziale.size() - 1);
		
		if (current.equals(target)) {
			int score = getScore(parziale);
			if (score > bestScore) {
				this.bestScore = score;
				this.bestPath = new ArrayList<Album>(parziale);
				return;
			}
		}
		
		
		for (Album a : successori) {
			DefaultWeightedEdge edge = this.graph.getEdge(current, a);
			double peso = this.graph.getEdgeWeight(edge);
			if (!parziale.contains(a) &&  peso >= treshold) {
				parziale.add(a);
				List<Album> newSuccessori = Graphs.successorListOf(this.graph, a);
				ricorsione(parziale, newSuccessori, treshold);
				parziale.remove(parziale.size()-1);
			}
		}
			
	}
	
	/*private void ricorsione(List<Album> parziale, Album target, int treshold) {
		// TODO Auto-generated method stub
		
		Album current = parziale.get(parziale.size() - 1);
		
		//condizione di uscita
		if (current.equals(target)) {
			//se vero, controllare se e' la soluzione migliore di best 
			if (getScore(parziale) > this.bestScore) {
				this.bestScore = getScore(parziale);
				this.bestPath = new ArrayList<>(parziale);
			}
			return;
		}
		
		//continuare ad aggiungere elementi in parziale
		List<Album> successors = Graphs.successorListOf(this.graph, current);
		
		for (Album a : successors) {
			if (this.graph.getEdgeWeight(this.graph.getEdge(current, a)) >= treshold) {
				//aggiungere il nodo a alla soluzione parziale
				parziale.add(a);
				ricorsione(parziale, target, treshold);
				parziale.remove(a);
				
			}
			
		}
		
	}*/


	private int getScore(List<Album> parziale) {
		// TODO Auto-generated method stub
		int score  =0;
		Album source = parziale.get(0);
		
		for (Album a : parziale.subList(1, parziale.size())) {
			if (getBilancio(a) > getBilancio(source)) {
				score += 1;
			}
		}
		return score;
	}


	public List<BilancioAlbum> getAdiacenti(Album album) {
		
		//fornisce tutti i nodi che vengono dopo --> uscenti da album
		List<Album> successori = Graphs.successorListOf(this.graph, album); //no neighbour perche' fornisce anche quelli entranti
		List<BilancioAlbum> bilancioSuccessori = new ArrayList<>();
		
		for (Album a : successori) {
			bilancioSuccessori.add(new BilancioAlbum(a, getBilancio(a)));
		}
		
		Collections.sort(bilancioSuccessori);
		return bilancioSuccessori;
		
	}
	
	public void loadNodes(int n) {
		if (allAlbum.isEmpty()) {
			this.allAlbum = dao.getFilteredlAlbums(n);
		}
		
		
		
	}
	
	public void buildGraph(int n) {
		
		clearGraph();
		
		loadNodes(n);
		
		Graphs.addAllVertices(this.graph, this.allAlbum);
		
		for (Album a1 : this.allAlbum) {
			for (Album a2 : this.allAlbum) {
				int peso  = a1.getNumSongs() - a2.getNumSongs();
				
				if ( peso > 0) {
					//arco va da a2 a a1
					Graphs.addEdgeWithVertices(this.graph, a2, a1, peso);
					
				}
			}
		}
		
		System.out.println(this.graph.vertexSet().size());
		System.out.println(this.graph.edgeSet().size());
	}
	
	private int getBilancio(Album album) {
		
		int bilancio = 0;
		//restituisce tutti gli edge che ENTRANO in album
		List<DefaultWeightedEdge> edgesin  = new ArrayList<>(this.graph.incomingEdgesOf(album))  ; 
		List<DefaultWeightedEdge> edgesOut = new ArrayList<>(this.graph.outgoingEdgesOf(album));
		
		for (DefaultWeightedEdge edge : edgesin) {
			bilancio += this.graph.getEdgeWeight(edge);
		}
		
		for (DefaultWeightedEdge edge : edgesOut) {
			bilancio -= this.graph.getEdgeWeight(edge);
		}
		
		return bilancio;
	}
	
	public List<Album> getVertices() {
		List<Album> allVertices = new ArrayList<>(this.graph.vertexSet());
		Collections.sort(allVertices);
		return allVertices;
	}

	//assicura di cancellare cio' che c'e' nel grafo per riempirlo tutto di nuovo
	private void clearGraph() {
		// TODO Auto-generated method stub
		allAlbum = new ArrayList<>();
		this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
	}

	public int getNumVertices() {
		// TODO Auto-generated method stub
		return this.graph.vertexSet().size();
	}

	public int getNumEdges() {
		// TODO Auto-generated method stub
		return this.graph.edgeSet().size();
	}
	
}
