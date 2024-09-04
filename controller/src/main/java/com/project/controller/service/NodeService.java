package com.project.controller.service;

import com.project.controller.entity.Node;
import com.project.controller.model.NodeInput;
import com.project.controller.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NodeService {

    @Autowired
    protected NodeRepository nodeRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<Long, Long> createNodesWithMap(List<NodeInput> inputNodes, Long graphId) {
//        nodes get new ids in db
        final var map = new HashMap<Long, Long>();
        inputNodes.forEach(node -> {
            final var newNode = new Node();
            newNode.setFilename(node.filename());
            newNode.setImage(node.image());
            newNode.setIsRoot(node.isRoot());
            newNode.setIncomingNodes(new HashSet<>());
            newNode.setOutgoingNodes(new HashSet<>());
            newNode.setGraphId(graphId);
            nodeRepository.save(newNode);
            map.put(node.id(), newNode.getId());
        });
        return map;
    }
}
