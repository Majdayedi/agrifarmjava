-- Create the commentaire table
DROP TABLE IF EXISTS commentaire;
CREATE TABLE IF NOT EXISTS commentaire (
    id INT PRIMARY KEY AUTO_INCREMENT,
    article_id INT NOT NULL,
    rate INT NOT NULL CHECK (rate BETWEEN 1 AND 5),
    commentaire TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE
); 